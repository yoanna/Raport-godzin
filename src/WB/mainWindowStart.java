package WB;

import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.itextpdf.text.DocumentException;

import PDF.PDF_Godziny;
import PDF.PDF_Godziny_wgMaszyn;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.ImageIcon;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class mainWindowStart extends JFrame {

	private JPanel contentPane;
	private JTextField dataRozpoczecia;
	private JTextField dataZakonczenia;
	private JTextField iloscGodzin;
	private JLabel lblWprowadDzieRozpoczcia;
	private JLabel lblWprowadDzieZakoczenia;
	private JLabel lblWprowadOczekiwanLiczb;

	
	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainWindowStart frame = new mainWindowStart();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	

	/**
	 * Create the frame.
	 */
	public mainWindowStart() {
		setResizable(false);
		setTitle("Menu");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 420, 420);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 153, 204));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		dataRozpoczecia = new JTextField();
		dataRozpoczecia.setBounds(132, 48, 138, 38);
		contentPane.add(dataRozpoczecia);
		dataRozpoczecia.setColumns(10);
		
		dataZakonczenia = new JTextField();
		dataZakonczenia.setBounds(132, 146, 138, 38);
		contentPane.add(dataZakonczenia);
		dataZakonczenia.setColumns(10);
		
		iloscGodzin = new JTextField();
		iloscGodzin.setBounds(132, 238, 138, 38);
		contentPane.add(iloscGodzin);
		iloscGodzin.setColumns(10);
		
		lblWprowadDzieRozpoczcia = new JLabel("Wprowad\u017A dzie\u0144 rozpocz\u0119cia analizy, format: rrrr-MM-dd");
		lblWprowadDzieRozpoczcia.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblWprowadDzieRozpoczcia.setBounds(24, 0, 369, 57);
		contentPane.add(lblWprowadDzieRozpoczcia);
		
		lblWprowadDzieZakoczenia = new JLabel("Wprowad\u017A dzie\u0144 zako\u0144czenia analizy, format: rrrr-MM-dd");
		lblWprowadDzieZakoczenia.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblWprowadDzieZakoczenia.setBounds(24, 97, 369, 38);
		contentPane.add(lblWprowadDzieZakoczenia);
		
		lblWprowadOczekiwanLiczb = new JLabel("Wprowad\u017A oczekiwan\u0105 liczb\u0119 godzin roboczych w przedziale");
		lblWprowadOczekiwanLiczb.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblWprowadOczekiwanLiczb.setBounds(23, 195, 370, 44);
		contentPane.add(lblWprowadOczekiwanLiczb);
		
		JButton analizaGodzin = new JButton("Rozpocznij analiz\u0119 pracownik\u00F3w");
		analizaGodzin.setFont(new Font("Tahoma", Font.PLAIN, 11));
		analizaGodzin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				String dataRozp = dataRozpoczecia.getText();
				String dataZak = dataZakonczenia.getText();
				String ileGodzin = iloscGodzin.getText();
				
				//sprawdzenie daty rozpoczecia
				if(!checkDatePattern(dataRozp)){
					JOptionPane.showMessageDialog(null, "Wprowadz poprawny format daty rrrr-mm-dd");
					dataRozpoczecia.setText("");
				}
				
				//sprawdzenie daty zakonczenia
				else if(!checkDatePattern(dataZak)){
					JOptionPane.showMessageDialog(null, "Wprowadz poprawny format daty rrrr-mm-dd");
					dataZakonczenia.setText("");
				}
				else if(!isInteger(ileGodzin)){
					JOptionPane.showMessageDialog(null, "Wprowadz poprawnie ilosc godzin");
					iloscGodzin.setText("");
				}
				else{
					//ile godzin
					int godziny = Integer.parseInt(ileGodzin);
					try {
						PDF_Godziny.createWeekRaport(dataRozp, dataZak, godziny);
						System.exit(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		analizaGodzin.setBounds(89, 287, 232, 38);
		contentPane.add(analizaGodzin);
		Image img = new ImageIcon(this.getClass().getResource("/BackgroundImage.jpg")).getImage();
		
		JButton btnRozpocznijAnalizMaszyn = new JButton("Rozpocznij analiz\u0119 maszyn");
		btnRozpocznijAnalizMaszyn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String dataRozp = dataRozpoczecia.getText();
				String dataZak = dataZakonczenia.getText();
				
				//sprawdzenie daty rozpoczecia
				if(!checkDatePattern(dataRozp)){
					JOptionPane.showMessageDialog(null, "Wprowadz poprawny format daty rrrr-mm-dd");
					dataRozpoczecia.setText("");
				}
				
				//sprawdzenie daty zakonczenia
				else if(!checkDatePattern(dataZak)){
					JOptionPane.showMessageDialog(null, "Wprowadz poprawny format daty rrrr-mm-dd");
					dataZakonczenia.setText("");
				}
				else{
					//ile godzin
					try {
						PDF_Godziny_wgMaszyn.Machines(dataRozp, dataZak);
						System.exit(0);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnRozpocznijAnalizMaszyn.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnRozpocznijAnalizMaszyn.setBounds(89, 343, 232, 38);
		contentPane.add(btnRozpocznijAnalizMaszyn);
		
		
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(img));
		lblNewLabel.setBounds(-383, -402, 812, 812);
		contentPane.add(lblNewLabel);
	}
	
	private static boolean checkDatePattern(String data) {
	    try {
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	        format.parse(data);
	        return true;
	    } catch (ParseException e) {
	        return false;
	    }
	}
	private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}
}