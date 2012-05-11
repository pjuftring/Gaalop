package de.gaalop.tba;

import de.gaalop.algebra.AlStrategy;
import de.gaalop.tba.table.BitIO.BitReader;
import de.gaalop.tba.table.BitIO.BitWriter;
import de.gaalop.tba.table.TableCompressed;
import de.gaalop.tba.table.TableHumanReadable;
import de.gaalop.tba.table.TableReaderIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides methods for loading a Multiplication table from a file
 * @author Christian Steinmetz
 */
public class MultTableLoader {

    /**
     * Loads an algebra from different files
     * @param useAlgebra The algebra to be used
     * @param filename_Products The filename of the file which contains the products
     * @param useAsRessource true, if filename_products is a ressource
     * @throws IOException
     */
    public void load(UseAlgebra useAlgebra, String filename_Products, boolean useAsRessource) throws IOException {

        int bladeCount = useAlgebra.getAlgebra().getBladeCount();

        IMultTable tableInner = useAlgebra.getTableInner();
        IMultTable tableOuter = useAlgebra.getTableOuter();
        IMultTable tableGeo = useAlgebra.getTableGeo();

        tableInner.createTable(bladeCount);
        tableOuter.createTable(bladeCount);
        tableGeo.createTable(bladeCount);

        TableReaderIO io = new TableCompressed(new BitReader(), new BitWriter());

        InputStream filestream;
        if (useAsRessource) {
            filestream = AlStrategy.class.getResourceAsStream(filename_Products);
        } else {
            filestream = new FileInputStream(new File(filename_Products));
        }

        io.readFromFile(filestream, tableInner, tableOuter, tableGeo);

    }
}
