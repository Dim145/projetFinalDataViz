import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Main extends PApplet
{
    // Le https ne fonctionnant pas avec processing, je prend les fichiers dans les ressources... cela ne change pas grand choses
    private static final String[] DEP_URL = {"resultats-par-niveau-dpt-t1-france-entiere.csv", "resultats-par-niveau-dpt-t2-france-entiere.txt"};
    private static final String[] REG_URL = {"resultats-par-niveau-reg-t1-france-entiere.txt", "resultats-par-niveau-reg-t2-france-entiere.txt"};

    private static final float RATIO = 2;

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

        for (int i = 0; i < datas.getRowCount()-1; i++)
        {
            int baseCoordColIndex = isDepartement ? 2 : 1;

            float x = datas.getInt(i+1, baseCoordColIndex) / RATIO;
            float y = datas.getInt(i+1, baseCoordColIndex + 1) / RATIO;
            float diametre = datas.getInt(i+1, baseCoordColIndex + 2) / RATIO;

            if(Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2)) < diametre/2)
            {
                fill(0);
                text(datas.getString(i+1, isDepartement ? 1 : 0), mouseX, mouseY);
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
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Table fetchRoundDatas() throws IOException
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

            String firstLine = "code\t";

            for (int i = 0; i <= nbCandidats; i++)
            {
                firstLine += "Nom prénom\t% voix" + (i < nbCandidats ? "\t" : "\n");
            }

            writer.write(firstLine);

            boolean isFirst = true;
            for (String line : lignes)
            {
                if(isFirst)
                {
                    isFirst = false;
                    continue;
                }

                String[] cols = line.split(";");

                String newLine = cols[0] + "\t";
                int index = 18;

                for (int i = 0; i <= nbCandidats; i++)
                {
                    newLine += cols[index] + " " + cols[index+1] + "\t";

                    newLine += cols[index+3] + "\t";

                    index += 6;
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
