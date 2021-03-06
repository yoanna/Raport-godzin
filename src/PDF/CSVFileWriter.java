package PDF;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CSVFileWriter {
	//Delimiter used in CSV file
	private static String DELIMITER = ";";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	static FileWriter fileWriter = null;
	
	public static void nowyPlik(String tytul, String delimiter, String[] headers, int ileRzedow, String[][] dane, String[][] naglowek){
		DELIMITER = delimiter;
		SimpleDateFormat doNazwy = new SimpleDateFormat("yyyy.MM.dd");
		SimpleDateFormat godzina = new SimpleDateFormat("HHmm");
		Calendar date = Calendar.getInstance();
		
		try {
			File theDir = new File(Parameters.getPathToSaveHours()+"/"+doNazwy.format(date.getTime()));
			// if the directory does not exist, create it
			if (!theDir.exists()) {
			    try{
			        theDir.mkdir();
			    } 
			    catch(SecurityException se){
			        //handle it
			    }
			}
			fileWriter = new FileWriter(Parameters.getPathToSaveHours()+"/"+doNazwy.format(date.getTime())+"/"+godzina.format(date.getTime())+tytul+".csv");
			
			for(int i = 0; i<naglowek[0].length; i++){
				for(int j  = 0; j<naglowek.length; j++){
					fileWriter.append(naglowek[j][i]);
					fileWriter.append(DELIMITER);
				}
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
			
			for(int i = 0; i<headers.length; i++){
				fileWriter.append(headers[i]);
				fileWriter.append(DELIMITER);
			}
			fileWriter.append(NEW_LINE_SEPARATOR);
			for(int i = 0; i<ileRzedow; i++){
				for(int j = 0; j<headers.length; j++){
					fileWriter.append(dane[j][i]);
					fileWriter.append(DELIMITER);
				}
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	
}
