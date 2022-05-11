import processing.core.PApplet;

import java.io.PrintWriter;

public class Table
{
    private String[][] data;
    private int rowCount;
    //  int nbColumn;

    private Main main;

    public Table(Main main)
    {
        data = new String[10][10];
        this.main = main;
    }

    public Table(Main main, String filename)
    {
        this.main = main;
        String[] rows = this.main.loadStrings(filename);
        data = new String[rows.length][];
        //    nbColumn = rows.length;

        for (int i = 0; i < rows.length; i++)
        {
            if (PApplet.trim(rows[i]).length() == 0)
            {
                continue; // skip empty rows
            }
            if (rows[i].startsWith("#"))
            {
                continue;  // skip comment lines
            }

            // split the row on the tabs
            String[] pieces = PApplet.split(rows[i], PApplet.TAB);
            // copy to the table array
            data[rowCount] = pieces;
            rowCount++;

            // this could be done in one fell swoop via:
            //data[rowCount++] = split(rows[i], TAB);
        }
        // resize the 'data' array as necessary
        data = (String[][]) PApplet.subset(data, 0, rowCount);
    }


    public int getRowCount()
    {
        return rowCount;
    }


    // find a row by its name, returns -1 if no row found
    public int getRowIndex(String name)
    {
        for (int i = 0; i < rowCount; i++)
        {
            if (data[i][0].equals(name))
            {
                return i;
            }
        }
        PApplet.println("No row named '" + name + "' was found");
        return -1;
    }


    public String getRowName(int row)
    {
        return getString(row, 0);
    }

    //  int getNbCol(int row) {
    //    return nbColumn;
    //  }

    public String getString(int rowIndex, int column)
    {
        return data[rowIndex][column];
    }


    public String getString(String rowName, int column)
    {
        return getString(getRowIndex(rowName), column);
    }


    public int getInt(String rowName, int column)
    {
        return PApplet.parseInt(getString(rowName, column));
    }


    public int getInt(int rowIndex, int column)
    {
        return PApplet.parseInt(getString(rowIndex, column));
    }


    public float getFloat(String rowName, int column)
    {
        return PApplet.parseFloat(getString(rowName, column));
    }


    public float getFloat(int rowIndex, int column)
    {
        return PApplet.parseFloat(getString(rowIndex, column));
    }


    public void setRowName(int row, String what)
    {
        data[row][0] = what;
    }


    public void setString(int rowIndex, int column, String what)
    {
        data[rowIndex][column] = what;
    }


    public void setString(String rowName, int column, String what)
    {
        int rowIndex = getRowIndex(rowName);
        data[rowIndex][column] = what;
    }


    public void setInt(int rowIndex, int column, int what)
    {
        data[rowIndex][column] = PApplet.str(what);
    }


    public void setInt(String rowName, int column, int what)
    {
        int rowIndex = getRowIndex(rowName);
        data[rowIndex][column] = PApplet.str(what);
    }


    public void setFloat(int rowIndex, int column, float what)
    {
        data[rowIndex][column] = PApplet.str(what);
    }


    public void setFloat(String rowName, int column, float what)
    {
        int rowIndex = getRowIndex(rowName);
        data[rowIndex][column] = PApplet.str(what);
    }


    // Write this table as a TSV file
    public void write(PrintWriter writer)
    {
        for (int i = 0; i < rowCount; i++)
        {
            for (int j = 0; j < data[i].length; j++)
            {
                if (j != 0)
                {
                    writer.print(PApplet.TAB);
                }
                if (data[i][j] != null)
                {
                    writer.print(data[i][j]);
                }
            }
            writer.println();
        }
        writer.flush();
    }
}
