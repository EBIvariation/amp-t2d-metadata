package uk.ac.ebi.ampt2d.metadata.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SraToAmpt2dFileConverterTest {

    @Mock
    private AnalysisFileType analysisFileType;

    @Test
    public void testFileConverter() {
        initializeAnalysisFileType();
        SraToAmpt2dConverter<AnalysisFileType,File> fileTypeConverter = new FileTypeConverter();
        File file = fileTypeConverter.convert(analysisFileType);
        assertAnalysisFileTypeEqualsFile(analysisFileType, file);
    }

    private void initializeAnalysisFileType() {
        when(analysisFileType.getFilename()).thenReturn("EGAF00001");
        when(analysisFileType.getFiletype()).thenReturn(AnalysisFileType.Filetype.VCF);
        when(analysisFileType.getUnencryptedChecksum()).thenReturn("checksum");
    }

    private void assertAnalysisFileTypeEqualsFile(AnalysisFileType analysisFileType, File file) {
        assertEquals(analysisFileType.getFilename(), file.getName());
        assertEquals(analysisFileType.getFiletype().toString().toUpperCase(), file.getType().toString());
        assertEquals(analysisFileType.getUnencryptedChecksum(), file.getHash());
    }

}