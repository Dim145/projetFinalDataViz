import processing.core.PApplet;
import processing.core.PImage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Main extends PApplet
{
    // Le https ne fonctionnant pas avec processing, je prend les fichiers dans les ressources... cela ne change pas grand choses
    private static final String[] DEP_URL = {"resultats-par-niveau-dpt-t1-france-entiere.txt", "resultats-par-niveau-dpt-t2-france-entiere.txt"};
    private static final String[] REG_URL = {"resultats-par-niveau-reg-t1-france-entiere.txt", "resultats-par-niveau-reg-t2-france-entiere.txt"};

    private static final Color[] COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.PINK, Color.ORANGE, Color.YELLOW, Color.CYAN, Color.MAGENTA,
                                            new Color(147, 0, 255), new Color(25, 222, 184), new Color(215, 0, 92), new Color(
            255, 157, 75)};

    public static final float RATIO = 2;
    public static final float DIAMETRE_CAMEMBERT = 200;
    public static final float RAYON_CAMEMBERT = DIAMETRE_CAMEMBERT / 2;

    private boolean isDepartement = true;

    private PImage background = null;
    private Table datas = null;
    private Table percentDatas = null;

    private int currentTour = 1;

    @Override
    public void settings()
    {
        size((int) (1138 / RATIO), (int) (1080 / RATIO), P2D);
    }

    @Override
    public void setup()
    {
        loadDatas();
    }

    @Override
    public void draw()
    {
        background(255);
        image(background, 0, 0, width, height);

        String tmpText = "Tour: " + this.currentTour + "\nVue " + (isDepartement ? "départementale" : "régionnale");

        fill(0);
        text(tmpText, width - 5 - textWidth(tmpText), height - textAscent()*2 - 5);

        tmpText = "cliquez pour changer de tour.\nappuyez sur espace pour changer de vue.";

        text(tmpText, 5, height - textAscent()*2 - 5);

        for (int i = 1; i < datas.getRowCount(); i++)
        {
            int baseCoordColIndex = isDepartement ? 2 : 1;

            float x = datas.getInt(i, baseCoordColIndex) / RATIO;
            float y = datas.getInt(i, baseCoordColIndex + 1) / RATIO;
            float diametre = datas.getInt(i, baseCoordColIndex + 2) / RATIO;

            if(Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2)) < diametre/2)
            {
                String text = datas.getString(i, isDepartement ? 1 : 0);
                fill(255);
                rect(mouseX - 1, mouseY - textAscent(), textWidth(text) + 2, textAscent()*1.5f);

                fill(0);
                text(text, mouseX, mouseY);

                drawCamenbert(i);
            }
        }
    }

    private void loadDatas()
    {
        background = loadImage(isDepartement ? "France_départementale.png" : "france_departementale.jpg");
        datas = new Table(this, isDepartement ? "departements-francais.tsv" : "regions-francaises.tsv");

        try
        {
            percentDatas = fetchRoundDatas();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void drawCamenbert(int lig)
    {
        float camCenterX = mouseX + RAYON_CAMEMBERT;
        float camCenterY = mouseY + RAYON_CAMEMBERT;

        if(camCenterY + RAYON_CAMEMBERT + 20 > height)
            camCenterY = mouseY - RAYON_CAMEMBERT;

        if(camCenterX + RAYON_CAMEMBERT + 20 > width)
            camCenterX = mouseX - RAYON_CAMEMBERT;

        float total = 0;
        int index = 3;

        String code = datas.getString(lig, isDepartement ? 0 : 9);

        try
        {
            code = switch (code)
            {
                case "971" -> "ZA";
                case "972" -> "ZB";
                case "973" -> "ZC";
                case "974" -> "ZD";
                case "976" -> "ZM";
                default -> String.format("%02d", Integer.parseInt(code));
            };
        }
        catch (NumberFormatException ignored)
        {

        }

        List<TextPos> textPosList = new ArrayList<>();

        int i2 = percentDatas.getRowIndex(code);
        while(true) try
        {
            float val = percentDatas.getFloat(i2, index);
            if(Float.isNaN(val)) break;

            val = PApplet.map(val, 0, 100, 0, 360);

            fill(COLORS[(index/3) % COLORS.length].getRGB());
            arc(camCenterX, camCenterY, DIAMETRE_CAMEMBERT, DIAMETRE_CAMEMBERT, PApplet.radians(total), PApplet.radians(total+val));

            if( val > (360 / 10f))
            {
                fill(0);
                float degText = total + (val / 2);

                String text = percentDatas.getString(i2, index-1);

                float sinVal = sin(PApplet.radians(degText));
                float cosVal = cos(PApplet.radians(degText));

                text(text,
                        camCenterX + ((RAYON_CAMEMBERT + 10) * cosVal) - (cosVal < 0 ? textWidth(text) : 0),
                        camCenterY + ((RAYON_CAMEMBERT + 10) * sinVal));

                text = percentDatas.getString(i2, index-2);

                float posNameX = camCenterX + ((RAYON_CAMEMBERT/2 + 10) * cosVal) - (cosVal < 0 ? textWidth(text) : 0);
                float posNameY = camCenterY + ((RAYON_CAMEMBERT/2 + 10) * sinVal);

                textPosList.add(new TextPos(text, posNameX, posNameY, COLORS[(index / 3) % COLORS.length]));
            }


            total += val;
            index += 3;
        }
        catch (Exception e)
        {
            break;
        }

        if(total > 0)
        {
            fill(Color.WHITE.getRGB());
            arc(camCenterX, camCenterY, DIAMETRE_CAMEMBERT, DIAMETRE_CAMEMBERT, PApplet.radians(total), PApplet.radians(total + (360 - total)));

            drawTextCamembert(camCenterX, camCenterY, total, 360 - total, "Vote blanc ou non voté");
        }

        for (TextPos tp : textPosList)
        {
            fill(tp.color.getRGB());
            rect(tp.x - 1, tp.y - textAscent(), textWidth(tp.text) + 2, textAscent()*1.5f);

            fill(0);
            text(tp.text, tp.x, tp.y);
        }
    }

    private void drawTextCamembert(float x, float y, float total, float currentAngleValue, String text)
    {
        fill(0);
        float degText = total + (currentAngleValue / 2);

        float sinVal = sin(PApplet.radians(degText));
        float cosVal = cos(PApplet.radians(degText));

        text(text,
                x + ((RAYON_CAMEMBERT + 10) * cosVal) - (cosVal < 0 ? textWidth(text) : 0),
                y + ((RAYON_CAMEMBERT + 10) * sinVal));
    }

    private static class TextPos
    {
        public String text;
        public float x;
        public float y;
        public Color color;

        public TextPos(String text, float x, float y, Color color)
        {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private Table fetchRoundDatas()
    {
        String nom = "france-" + (isDepartement ? "departements" : "regions") + "-tour-" + this.currentTour + ".tsv";

        String chemin = dataPath(nom);
        File   file = new File(chemin);

        if (!file.exists() || file.length() == 0)
        {
            PrintWriter writer = createWriter(chemin);

            String url = (isDepartement ? DEP_URL : REG_URL )[currentTour-1];

            String[] lignes = loadStrings(url);

            int nbCandidats = currentTour == 1 ? 11 : 1; // 0 inclus

            StringBuilder firstLine = new StringBuilder("code\t");

            for (int i = 0; i <= nbCandidats; i++)
            {
                firstLine.append("Nom prénom\tnb voix\t% voix").append(i < nbCandidats ? "\t" : "\n");
            }

            writer.write(firstLine.toString());

            boolean isFirst = true;
            for (String line : lignes)
            {
                if(isFirst)
                {
                    isFirst = false;
                    continue;
                }

                String[] cols = line.split(";");

                StringBuilder newLine = new StringBuilder(cols[0] + "\t");
                int index = 18;

                for (int i = 0; i <= nbCandidats; i++)
                {
                    for (int j = 0; j < 4; j++)
                        newLine.append(cols[index++]).append(j == 0 ? " " : "\t");

                    index += 2;
                }

                writer.write(newLine + "\n");
            }

            writer.flush();
            writer.close();
        }

        return new Table(this, chemin);
    }

    @Override
    public void keyPressed()
    {
        if(key == ' ')
        {
            this.isDepartement = !this.isDepartement;
            loadDatas();
        }
    }

    @Override
    public void mouseClicked()
    {
        currentTour = 3 - currentTour;
        loadDatas();
    }

    public static void main(String[] args)
    {
        PApplet.main(Main.class);
    }
}
