package PDF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Section;

public class PDF_Godziny {
static Connection connection= WB.Connection2DB.dbConnector();
	
	private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
	        Font.BOLD);
	private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
	        Font.NORMAL, BaseColor.RED);
	private static Font smallFont = new Font(Font.FontFamily.TIMES_ROMAN, 8,
	        Font.BOLD);
	private static Font smallFont2 = new Font(Font.FontFamily.TIMES_ROMAN, 8);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
	        Font.BOLD);
	

	public static void createWeekRaport(String start, String end, int Hours) throws SQLException, FileNotFoundException, DocumentException{
		
		Document doc = new Document();
		SimpleDateFormat doNazwy = new SimpleDateFormat("yyyy.MM.dd");
		SimpleDateFormat godz = new SimpleDateFormat("HH;mm");
		Calendar date = Calendar.getInstance();
		String path = Parameters.getPathToSaveHours()+"/"+doNazwy.format(date.getTime())+"/";
		
		File theDir = new File(path);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    try{
		        theDir.mkdir();
		    } 
		    catch(SecurityException se){
		        //handle it
		    }
		}
		
		int ilePracownikow=1;
		String [] headers = new String [10];
		Scanner input = new Scanner(System.in);
		int ileKolumn = headers.length;
		String [][] naglowek = new String [ileKolumn][2];
		
		headers[0] = "Nazwisko Imie";
		headers[1] = "Zarejestrowane [h]";
		headers[2] = "Teoretyczne [h]";
		headers[3] = "Zakonczonych serii";
		headers[4] = "Stanowiska";
		headers[5] =  "[1]  Teor/Zarejestr";
		headers[6] =  "[2]  Teor/Godzin pracy";
		headers[7] =  "[3]  Zarejestr/Godzin pracy";
		headers[8] =  "[2]*[3]";
		headers[9] =  "[1]*[3]";	
		
		String name = "Raport godzin od "+start+" do "+end+".pdf";
		File f = new File(path+name);
		if(f.exists() && !f.isDirectory())
			name = godz.format(date.getTime())+" "+name;
		PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path+name));
		doc.open();
		writer.setPageEvent(new PDF_MyFooter());
		
		//wybierz wszystkie gniazda
		String sql1 = "SELECT nest, cfnestoms from werkpost where nest<> 'KOP7' and nest <>\"\" group by nest, cfnestoms order by nest";
		String [][] tab = new String [5][12];
		int ktoreGniazdo = -1;
		Statement st1 = connection.createStatement();
		ResultSet rs1 = st1.executeQuery(sql1);
		int zarejestrowanychMinutAll = 0;
		int teoretycznychMinutAll = 0;
		double wydajnoscAll;
		while(rs1.next()){
			ktoreGniazdo++;
			String nest = rs1.getString("nest");
			int minutyWydzial = 0;
			int TminutyWydzial = 0;
			
			tab[0][ktoreGniazdo] = nest;
			tab[1][ktoreGniazdo] = rs1.getString("cfnestoms");
			
			//Zrobienie naglowka
			
			 Paragraph preface = new Paragraph();
             // We add one empty line
			 preface.add("\n");
             // Lets write a big header
             preface.add(new Paragraph("Raport godzin pracownik�w", catFont));

             preface.add("\n");
             // Will create: Report generated by: _name, _date
             preface.add(new Paragraph("Od: "+start+" do:  "+end+"  ilosc godzin w tygodniu: "+Hours, smallBold));
             preface.add("\n");
             preface.add(new Paragraph(
                             "Gniazdo:  "+nest+"  "+tab[1][ktoreGniazdo],
                             smallBold));

             preface.add("\n");
             doc.add(preface);
             
             PdfPTable tabPDF = new PdfPTable(ileKolumn);
             float widths[] = new float[] { 10, 6, 6, 6, 6, 8, 8, 8, 6, 6};
			
		//  ilosc pracownikow w okre�lonym przedziale czasowym
			String a = "select count(*) from (select cfnaam from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and cfnaam not like 'AWARIA%' and GniazdoS = '"+nest+"' and verwerkt = 1 group by cfnaam) M";
			Statement a1 = connection.createStatement();
			ResultSet rs2 = a1.executeQuery(a);
			while(rs2.next()){
				ilePracownikow= rs2.getInt(1);
			}
			a1.close();
			String[][] t = new String[ileKolumn][2*(ilePracownikow+1)];
			System.out.println(ilePracownikow);
			String b = null;

			int p = 0;
			b = "select cfnaam from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and cfnaam not like 'AWARIA%' and GniazdoS = '"+nest+"' and verwerkt = 1 group by cfnaam";
			Statement b1 = connection.createStatement();
			ResultSet rs3 = b1.executeQuery(b);
			
			//dla kazdego gniazda licz godziny (dla kazdego pracownika osobno)
			
			//DLA KAZDEGO PRACOWNIKA
			while(rs3.next()){
				String nazwisko = rs3.getString(1);
				if(nazwisko.equals("NAREGOWSKA MONIKA")) nazwisko = "KOREKTA BLEDOW";
				System.out.println(nazwisko);
				t[0][2*p] = nazwisko;
				t[0][(2*p)+1] = nazwisko;
				int ileMinutTeoret = 0;
				int MinutWykonano = 0;
				int ileSeriiUkonczono = 0;
				
				//z werkuren -> wszystkie rejestracje czasu pracownika w okreslonym przedziale czasowym W GNIEZDZIE
					//suma czasu dla jednego bonu 
				
				String sql2 = "select werkbon, sum(tijd), status from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and CFNAAM = '"+nazwisko+"' and GniazdoS = '"+nest+"' and verwerkt = 1  group by werkbon ";
				Statement stm1 = connection.createStatement();
				ResultSet rs4 = stm1.executeQuery(sql2);
				while(rs4.next()){
					//int GodzinDlaBonu = rs4.getInt(4);
					int MinutDlaBonu = rs4.getInt("SUM(TIJD)");
					MinutWykonano += MinutDlaBonu;
					String numerBonu = rs4.getString("werkbon");
					
					//wyszukanie czasu teoretycznego 
					String sql4 = "select hoeveelheid, instelminuten, werkminuten, status from werkbon where werkbonnummer = '"+numerBonu+"'";           
					Statement stm3 = connection.createStatement();
					//int ileHTeoria= 0;
					int ileMinTeoria = 0;
					int ileSztuk = 0;
					String status = rs4.getString("status");
					ResultSet rs5 = stm3.executeQuery(sql4);
					while(rs5.next()){
						ileSztuk = rs5.getInt(1);
						ileMinTeoria = rs5.getInt(2) + (rs5.getInt(3)*ileSztuk);
					}
					stm3.close();
				
					if(status.equals("90")){
						String sql10 = "select sum(tijd) from Rejestracja where ((datum < '"+start+"' and werkbon = '"+numerBonu+"') or (datum >= '"+start+"' and datum <= '"+end+"' and cfnaam <> '"+nazwisko+"' and werkbon = '"+numerBonu+"')) and verwerkt = 1";
						Statement stm4 = connection.createStatement();
						ResultSet rs6 = stm4.executeQuery(sql10);
						int godzOdejmij = 0;
						int minOdejmij = 0;
						ileSeriiUkonczono++;
						while(rs6.next()){
							minOdejmij = rs6.getInt(1);
						}
						stm4.close();
						ileMinTeoria -= minOdejmij;
						ileMinutTeoret += ileMinTeoria;
					}
					else if(status.equals("20")){
						ileMinutTeoret += MinutDlaBonu;
					}				
				}
				stm1.close();
				
				double ileGodzinTeoret = (double)ileMinutTeoret/60;
				double GodzinWykonano = (double)MinutWykonano/60;
				
				minutyWydzial+=MinutWykonano;
				TminutyWydzial += ileMinutTeoret;
				
				//Sprawdzenie ile maszyn
				String sql6 = "select werkpost from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and CFNAAM = '"+nazwisko+"' and GniazdoS = '"+nest+"' and verwerkt = 1 group by werkpost";
				Statement stm6 = connection.createStatement();
				ResultSet rs6 = stm6.executeQuery(sql6);
				String ileMaszyn = "";
				while(rs6.next()){
					ileMaszyn = ileMaszyn + rs6.getString(1) + ", ";
				}
				stm6.close();
				
				double p1, p2, p3, p4, p5;
				
				p1 = (double) ileGodzinTeoret/ (double)GodzinWykonano;
				p2 = (double)ileGodzinTeoret/(double)Hours;
				p3 = (double)GodzinWykonano/(double)Hours;
				p4 = p2*p3;
				p5 = p1*p3;
				
				t[1][2*p]="";
				t[2][2*p]="";
				
				if(ileMinutTeoret<0){
					t[2][2*p] = "-";
					ileMinutTeoret = ileMinutTeoret*(-1);
				}
				
				if(MinutWykonano<0){
					t[1][2*p] = "-";
					MinutWykonano = MinutWykonano*(-1);
				}
				
				if(MinutWykonano%60>=10)
					t[1][2*p] += Integer.toString(MinutWykonano/60)+":"+Integer.toString(MinutWykonano%60);
				else
					t[1][2*p] += Integer.toString(MinutWykonano/60)+":0"+Integer.toString(MinutWykonano%60);
				
				if(ileMinutTeoret%60>=10)
					t[2][2*p] += Integer.toString(ileMinutTeoret/60)+":"+Integer.toString(ileMinutTeoret%60);
				else
					t[2][2*p] += Integer.toString(ileMinutTeoret/60)+":"+Integer.toString(ileMinutTeoret%60);
				
				t[3][2*p] = Integer.toString(ileSeriiUkonczono);
				t[4][2*p] = ileMaszyn;
				t[5][2*p] = String.format("%.1f", p1*100) + "%";
				t[6][2*p] = String.format("%.1f", p2*100) + "%";
				t[7][2*p] = String.format("%.1f", p3*100) + "%";
				t[8][2*p] = String.format("%.1f", p4*100)+ "%";
				t[9][2*p] = String.format("%.1f", p5*100)+ "%";
				
				
				ileGodzinTeoret = 0;
				ileMinutTeoret = 0;
				GodzinWykonano = 0;
				MinutWykonano = 0;
				ileSeriiUkonczono = 0;
				
				//z Rejestracji -> wszystkie rejestracje czasu pracownika w okreslonym przedziale czasowym
					//suma czasu dla jednego bonu 
				// na WSZYSTKICH HALACH
				
				String sql10 = "select werkbon, sum(tijd), status from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and CFNAAM = '"+nazwisko+"' and verwerkt = 1 group by werkbon ";
				Statement stm10 = connection.createStatement();
				ResultSet rs10 = stm10.executeQuery(sql10);
				while(rs10.next()){
					int MinutDlaBonu = rs10.getInt(2);
					MinutWykonano += MinutDlaBonu;
					String numerBonu = rs10.getString(1);
					//wyszukanie czasu teoretycznego 
					String sql11 = "select hoeveelheid, instelminuten, werkminuten, status from werkbon where werkbonnummer = '"+numerBonu+"'";           
					Statement stm11 = connection.createStatement();
					int ileMinTeoria = 0;
					int ileSztuk = 0;
					String status = rs10.getString("status");
					ResultSet rs11 = stm11.executeQuery(sql11);
					while(rs11.next()){
						ileSztuk = rs11.getInt(1);
						ileMinTeoria = rs11.getInt(2) + (rs11.getInt(3)*ileSztuk);
					}
					stm11.close();
					
					if(status.equals("90")){
						String sql12 = "select sum(tijd) from Rejestracja where ((datum < '"+start+"' and werkbon = '"+numerBonu+"') or (datum >= '"+start+"' and datum <= '"+end+"' and cfnaam <> '"+nazwisko+"' and werkbon = '"+numerBonu+"')) and verwerkt = 1";
						Statement stm12 = connection.createStatement();
						ResultSet rs12 = stm12.executeQuery(sql12);
						int minOdejmij = 0;
						ileSeriiUkonczono++;
						while(rs12.next()){
							minOdejmij = rs12.getInt(1);
						}
						stm12.close();
						ileMinTeoria -= minOdejmij;
						ileMinutTeoret += ileMinTeoria;
					}
					else if(status.equals("20")){
						ileMinutTeoret += MinutDlaBonu;
					}				
				}
				stm10.close();
				
				 ileGodzinTeoret = (double)ileMinutTeoret/60;
				 GodzinWykonano = (double)MinutWykonano/60;
				
				//Sprawdzenie ile maszyn
				String sql13 = "select count(*) from (select werkpost from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and CFNAAM = '"+nazwisko+"' and verwerkt = 1 group by werkpost) M";
				Statement stm13 = connection.createStatement();
				ResultSet rs13 = stm13.executeQuery(sql13);
				int ileMaszyn2 = 0;
				while(rs13.next()){
					ileMaszyn2 = rs13.getInt(1);
				}
				stm13.close();
				
				double p12, p22, p32, p42, p52;
				
				p12 = (double) ileGodzinTeoret/ (double)GodzinWykonano;
				p22 = (double)ileGodzinTeoret/(double)Hours;
				p32 = (double)GodzinWykonano/(double)Hours;
				p42 = p22*p32;
				p52 = p12*p32;
				t[1][(2*p)+1]="";
				t[2][(2*p)+1]="";
				
				if(ileMinutTeoret<0){
					t[2][(2*p)+1] = "-";
					ileMinutTeoret = ileMinutTeoret*(-1);
				}
				
				if(MinutWykonano<0){
					t[1][(2*p)+1] = "-";
					MinutWykonano = MinutWykonano*(-1);
				}
				
				if(MinutWykonano%60>=10)
					t[1][(2*p)+1] += Integer.toString(MinutWykonano/60)+":"+Integer.toString(MinutWykonano%60);
				else
					t[1][(2*p)+1] += Integer.toString(MinutWykonano/60)+":0"+Integer.toString(MinutWykonano%60);
				if(ileMinutTeoret%60>=10)
					t[2][(2*p)+1] += Integer.toString(ileMinutTeoret/60)+":"+Integer.toString(ileMinutTeoret%60);
				else
					t[2][(2*p)+1] += Integer.toString(ileMinutTeoret/60)+":0"+Integer.toString(ileMinutTeoret%60);
				t[3][(2*p)+1] = Integer.toString(ileSeriiUkonczono);
				t[4][(2*p)+1] = Integer.toString(ileMaszyn2);
				t[5][(2*p)+1] = String.format("%.1f", p12*100) + "%";
				t[6][(2*p)+1] = String.format("%.1f", p22*100) + "%";
				t[7][(2*p)+1] = String.format("%.1f", p32*100) + "%";
				t[8][(2*p)+1] = String.format("%.1f", p42*100)+ "%";
				t[9][(2*p)+1] = String.format("%.1f", p52*100)+ "%";
				
				p++;
			}//koniec podsumowania pracownikow w gniezdzie
			
			for(int i = 0; i<2; i++){
				for(int j = 0; j<ileKolumn; j++){
					if(i==0)
					{
						if(j==2){
							naglowek[j][i] = "Data rozpoczecia";
						}
						else if(j==3)
							naglowek[j][i] = "Data zakonczenia";
						else if(j==4) naglowek [j][i] = "Ilosc godzin";
						else naglowek [j][i]="";
					}
					if(i==1)
					{
						if(j==2){
							naglowek[j][i] = start;
						}
						else if(j==3)
							naglowek[j][i] = end;
						else if(j==4) naglowek [j][i] = Integer.toString(Hours);
						else naglowek [j][i]="";
					}
				}
			}
			if(ilePracownikow>0)
				CSVFileWriter.nowyPlik("Godziny pracownikow "+nest.replace('/', '-'), ";", headers, 2*ilePracownikow, t, naglowek);
			
			//Zrobienie PDFa
			for(int i = 0; i<ileKolumn; i++){
				PdfPCell c1 = new PdfPCell(new Phrase(headers[i], smallFont));
				c1.setMinimumHeight(30);
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
				c1.setBackgroundColor(BaseColor.ORANGE);
				tabPDF.addCell(c1);
			}
			for(int i = 0; i<((2*ilePracownikow)); i++){
				for(int j = 0; j<ileKolumn; j++){
					String zawartosc = t[j][i];
					//dodaj tylko te kolumny ktore odnosza sie do wszystkich godzin pracownika:
					if(j>=5){
						if(i%2==0){
							zawartosc = t[j][i+1];
							if(zawartosc.equals("NaN%")||zawartosc.equals("Infinity%"))
								zawartosc = "";
							PdfPCell c2 = new PdfPCell(new Phrase(zawartosc, smallFont2));
							c2.setRowspan(2);
							c2.setHorizontalAlignment(Element.ALIGN_CENTER);
							c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
							tabPDF.addCell(c2);
						}
					}
					else{
						if(zawartosc.equals("NaN%")||zawartosc.equals("Infinity%"))
							zawartosc = "";
						PdfPCell c2 = new PdfPCell(new Phrase(zawartosc, smallFont2));
						c2.setFixedHeight(25);
						if(i%2==1)
							c2.setFixedHeight(15);
						if(j==0)
							c2.setRowspan(2);
						c2.setHorizontalAlignment(Element.ALIGN_CENTER);
						c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
						//nie dodawaj je�li drugi rz�d nazwiska
						if(!(j==0 && i%2==1))
							tabPDF.addCell(c2);
					}
				}
				
			}
			tabPDF.setWidths(widths);
			tabPDF.setHeaderRows(1);
			tabPDF.setWidthPercentage(100);
			tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
			tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
			doc.add(tabPDF);
			doc.newPage();
			
			double procent = ((double)TminutyWydzial)/((double)minutyWydzial);
			procent = procent*100;
			
			zarejestrowanychMinutAll +=minutyWydzial;
			teoretycznychMinutAll += TminutyWydzial;
			tab[2][ktoreGniazdo] = Integer.toString(minutyWydzial/60)+":"+Integer.toString(minutyWydzial%60);
			tab[3][ktoreGniazdo] = Integer.toString(TminutyWydzial/60)+":"+Integer.toString(TminutyWydzial%60);
			tab[4][ktoreGniazdo] = String.format("%.1f", procent)+ "%";
			
			
		}//koniec gniazda
		
		String sql = "select count(*) from (select cfnaam from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and cfnaam not like 'AWARIA%' and verwerkt = 1 group by cfnaam) M";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		String ile = "";
		while(rs.next()){
			ile = rs.getString(1);
		}
		rs.close();
		st.close();
		
		headers = new String[5];
		headers[0] = "Gniazdo";
		headers[1] = "Gniazdo - opis";
		headers[2] = "Czas zarejestrowany";
		headers[3] = "Czas teoretyczny";
		headers[4] = "Wydajnosc";
		
		CSVFileWriter.nowyPlik("Podsumowanie gniazd", ";", headers, 12, tab, naglowek);
		//dodaj do  tabeli zbiorcza ilosc godzin dla wydzialu)
		//drukuj do csv i do pdf
		
		
		Paragraph preface = new Paragraph();
        // We add one empty line
		 preface.add("\n");
        // Lets write a big header
        preface.add(new Paragraph("Raport godzin pracownik�w", catFont));
        
        preface.add("\n");
        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph("Od: "+start+" do:  "+end+"  ilosc godzin w tygodniu: "+Hours, smallBold));
        preface.add("\n");
        preface.add(new Paragraph("Ilosc pracownik�w zarejestrowanych: "+ile, smallBold));
        preface.add("\n");
        preface.add(new Paragraph(
                        "Raport wg gniazd",
                        smallBold));

        preface.add("\n");
        doc.add(preface);
		
		PdfPTable tab2PDF = new PdfPTable(5);
		 float widths[] = new float[] { 5, 14, 10, 10, 8};
		for(int i = 0; i<5; i++){
			PdfPCell c1 = new PdfPCell(new Phrase(headers[i], smallFont));
			c1.setMinimumHeight(30);
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			c1.setBackgroundColor(BaseColor.ORANGE);
			tab2PDF.addCell(c1);
		}
		for(int i = 0; i<12; i++){
			for(int j = 0; j<5; j++){
				String zawartosc = tab[j][i];
				if(zawartosc.equals("NaN%")||zawartosc.equals("Infinity%"))
					zawartosc = "";
				PdfPCell c2 = new PdfPCell(new Phrase(zawartosc, smallFont2));
				c2.setMinimumHeight(30);
				c2.setHorizontalAlignment(Element.ALIGN_CENTER);
				c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
				tab2PDF.addCell(c2);
			}
		}
		
		PdfPCell c1 = new PdfPCell(new Phrase("SUMA", smallFont2));
		c1.setMinimumHeight(30);
		c1.setColspan(2);
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
		tab2PDF.addCell(c1);
		
		PdfPCell c2 = new PdfPCell(new Phrase(Integer.toString(zarejestrowanychMinutAll/60)+":"+Integer.toString(zarejestrowanychMinutAll%60), smallFont2));
		c2.setMinimumHeight(30);
		c2.setHorizontalAlignment(Element.ALIGN_CENTER);
		c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
		tab2PDF.addCell(c2);
		
		PdfPCell c3 = new PdfPCell(new Phrase((Integer.toString(teoretycznychMinutAll/60)+":"+Integer.toString(teoretycznychMinutAll%60)), smallFont2));
		c3.setMinimumHeight(30);
		c3.setHorizontalAlignment(Element.ALIGN_CENTER);
		c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
		tab2PDF.addCell(c3);
		
		wydajnoscAll = (double)teoretycznychMinutAll*100/(double)zarejestrowanychMinutAll;
		
		PdfPCell c4 = new PdfPCell(new Phrase(String.format("%.1f", wydajnoscAll)+ "%", smallFont2));
		c4.setMinimumHeight(30);
		c4.setHorizontalAlignment(Element.ALIGN_CENTER);
		c4.setVerticalAlignment(Element.ALIGN_MIDDLE);
		tab2PDF.addCell(c4);
		
		tab2PDF.setWidthPercentage(100);
		tab2PDF.setWidths(widths);
		tab2PDF.setHorizontalAlignment(Element.ALIGN_CENTER);
		tab2PDF.setHorizontalAlignment(Element.ALIGN_CENTER);
		doc.add(tab2PDF);
		doc.close();
		
		return;
	}

	
	
}