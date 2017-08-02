package it.infocert.eigor.api;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchematronXSLTUpdateTest {

    public @Rule
    TemporaryFolder tmp = new TemporaryFolder();
    public @Rule
    TestName test = new TestName();

    private SchematronXSLTFileUpdater fileUpdater;
    private File schDirectory;
    private File xsltDirectory;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private File schFile;


    @Before
    public void setUpFolders() throws IOException {

        schDirectory = tmp.newFolder(test.getMethodName(), "schematron");

        xsltDirectory = tmp.newFolder(test.getMethodName(), "schematron-xslt");

        // copy an input schematron file in the schematron folder
        schFile = TestUtils.copyResourceToFolder("/simple.sch", schDirectory);

        fileUpdater = new SchematronXSLTFileUpdater(xsltDirectory.getAbsolutePath(), schDirectory.getAbsolutePath());

    }

    @Test
    public void ifXsltDirectoryIsEmptyUpdateIsNeeded() {
        assertTrue(fileUpdater.checkForUpdatedSchematron());
    }

    @Test
    public void ifXsltUpdateIsRunThenUpdateIsNotNeeded() {
        fileUpdater.updateXSLTfromSch();
        assertFalse(fileUpdater.checkForUpdatedSchematron());
    }

    @Test
    public void ifSchFileIsChangedThenUpdateIsNeeded() throws IOException, InterruptedException {
        fileUpdater.updateXSLTfromSch();
        boolean delete = schFile.delete();
        assertTrue(delete);

        // without sleep it sometimes happens that the generated xslt and the newly copied sch files will have
        // the same timestamp (down to the millisecond)
        Thread.sleep(1000);
        schFile = TestUtils.copyResourceToFolder("/simple.sch", schDirectory);

        assertTrue(fileUpdater.checkForUpdatedSchematron());
    }

    @Test
    public void ifUpdateIsRunThenXsltDirectoryShouldNotBeEmpty() throws IOException {
        fileUpdater.updateXSLTfromSch();
        File resultXslt = new File(xsltDirectory.getAbsolutePath() + "/simple.xslt");
        assertTrue(resultXslt.exists());
    }

}