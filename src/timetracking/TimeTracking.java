package timetracking;

import java.awt.EventQueue;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TimeTracking extends JFrame implements TableModelListener {
	private static final long serialVersionUID = 1L;
	private JPanel frame_container;
	private JTable tblTimeRecords;
	private DecimalFormat dfDuration = new DecimalFormat("#0.00");
	private DefaultTableModel TimeRecords;
	private boolean bDeletingActive = false;
	private boolean bImportActive = false;
	private ArrayList<ArrayList<String>> arr_sRecords = new ArrayList<ArrayList<String>>();
	static String connectURL = "jdbc:mysql://philnet.ch/philnet_zeiterfassung";
    static String user = "rbsoft_ze";
    static String pw = "RB5of1*J&va+lG1nn";
    static Connection conn;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TimeTracking frame = new TimeTracking();
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
	public TimeTracking() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				DBSave();
			}
		});
		setTitle("Timetracking");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 972, 565);
		setLocationRelativeTo(null);
		frame_container = new JPanel();
		frame_container.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(frame_container);
		frame_container.setLayout(null);
		
		JPanel panel_border1 = new JPanel();
		panel_border1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Edit table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel_border1.setBounds(12, 12, 788, 62);
		frame_container.add(panel_border1);
		panel_border1.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(12, 91, 932, 423);
		frame_container.add(scrollPane);
		
		
		
		/*
		 * Create table
		 */
		tblTimeRecords = new JTable();
		tblTimeRecords.setToolTipText("");
		TimeRecords = new DefaultTableModel(new Object[][] {{"", "", "", "", "", "", dfDuration.format(0)}},
				new String[] {"Date", "Employee", "Activity category", "Activity", "Start time", "End time", "Duration (hours)"});
		tblTimeRecords.setModel(TimeRecords);
		TimeRecords.addTableModelListener(this);
		
		//Set column widths
		tblTimeRecords.getColumnModel().getColumn(0).setPreferredWidth(69);
		tblTimeRecords.getColumnModel().getColumn(1).setPreferredWidth(81);
		tblTimeRecords.getColumnModel().getColumn(2).setPreferredWidth(93);
		tblTimeRecords.getColumnModel().getColumn(3).setPreferredWidth(201);
		tblTimeRecords.getColumnModel().getColumn(4).setPreferredWidth(58);
		tblTimeRecords.getColumnModel().getColumn(5).setPreferredWidth(46);
		
		
		
		/*
		 * Align start & end time right
		 */
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		tblTimeRecords.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		tblTimeRecords.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
		tblTimeRecords.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
		scrollPane.setViewportView(tblTimeRecords);
		
		/*
		 * Load data form database at launch
		 */
		DBLoad();
		TimeRecords.removeRow(0);
		
		
		JButton btnAddRow = new JButton("Add row");
		btnAddRow.setMnemonic('n');
		btnAddRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SetBuffer();
				
				SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
				String sDate = sdfDate.format(System.currentTimeMillis());
				
				TimeRecords.addRow(new Object[]{sDate, "", "", "", "", "", dfDuration.format(0)});
				tblTimeRecords.requestFocus();
				tblTimeRecords.editCellAt(tblTimeRecords.getRowCount() - 1, 1);
				
				RemBuffer();
			}
		});
		btnAddRow.setBounds(12, 23, 126, 26);
		panel_border1.add(btnAddRow);
		
		JButton btnRemoveRow = new JButton("Delete row");
		btnRemoveRow.setMnemonic('l');
		btnRemoveRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(tblTimeRecords.getSelectedRow() == -1) {return;} else {
					bDeletingActive = true;
					int iSelRow = tblTimeRecords.getSelectedRow();
					TimeRecords.removeRow(iSelRow);
					bDeletingActive = false;
				}
			}
		});
		btnRemoveRow.setBounds(150, 23, 126, 26);
		panel_border1.add(btnRemoveRow);
		
		JButton btnSave = new JButton("Save to database");
		btnSave.setMnemonic('s');
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame_container.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				DBSave();
				frame_container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				JOptionPane.showMessageDialog(null, "All entries were saved in the database!", "Database save successful", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		btnSave.setBounds(288, 23, 212, 26);
		panel_border1.add(btnSave);
		
		
		
		/*
		 * Import entries
		 */
		JButton btnImport = new JButton("Import");
		btnImport.setMnemonic('i');
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Create and show file chooser for the import file
				JFileChooser fcImport = new JFileChooser();
				fcImport.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fcImport.setFileFilter(new FileNameExtensionFilter("CSV files \".csv\"", "csv"));
				fcImport.setCurrentDirectory(new File("$HOME"));
				int iStatus = fcImport.showOpenDialog(null);
				
				//Cancel the import if the Dialog was canceled
				if(iStatus == JFileChooser.APPROVE_OPTION) {
					
					//Create file handle for the import file
					String sImportFileName = fcImport.getSelectedFile().getAbsolutePath();
					File ImportFile = new File(sImportFileName);
					BufferedReader brImport = null;
					
					//Show error if file doesn't exist
					if(!ImportFile.exists()){
						JOptionPane.showMessageDialog(null, "Please choose a valid file/Enter a valid file path!", "File error", JOptionPane.ERROR_MESSAGE);
					} else {
						//set buffer row in table
						SetBuffer();
						
						//Supress errors for incomplete entries while import is active
						bImportActive = true;
						try {
							//Create stream to read file
							brImport = new BufferedReader(new FileReader(sImportFileName));
							String sRecord;
							
							//Read next line of file until it ends
							while((sRecord = brImport.readLine()) != null) {
								
								//Store data in array
								ArrayList<String> arr_sRecord = new ArrayList<String>(Arrays.asList(sRecord.split(";")));
								arr_sRecords.add(arr_sRecord);
								
								/*
								 * Compare each row with existing one to avoid duplicates
								 */
								for(int iRow = 0; iRow < TimeRecords.getRowCount(); iRow++) {
									//Compare values with current row
									boolean bIsDuplicate = 
											arr_sRecords.get(iRow).get(0).equals((String) TimeRecords.getValueAt(iRow, 0)) &
											arr_sRecords.get(iRow).get(1).equals((String) TimeRecords.getValueAt(iRow, 1)) &
											arr_sRecords.get(iRow).get(2).equals((String) TimeRecords.getValueAt(iRow, 2)) &
											arr_sRecords.get(iRow).get(3).equals((String) TimeRecords.getValueAt(iRow, 3)) &
											arr_sRecords.get(iRow).get(4).equals((String) TimeRecords.getValueAt(iRow, 4)) &
											arr_sRecords.get(iRow).get(5).equals((String) TimeRecords.getValueAt(iRow, 5)) &
											arr_sRecords.get(iRow).get(6).equals((String) TimeRecords.getValueAt(iRow, 6));
									
									//Stop duplicate control and setting flag
									if(bIsDuplicate) {
										arr_sRecords.get(arr_sRecords.indexOf(arr_sRecord)).add(7, "true");
										break;
									} else {
										arr_sRecords.get(arr_sRecords.indexOf(arr_sRecord)).add(7, "false");
									}
								}
							}
							
							//Add all records which aren't duplicates
							for(ArrayList<String> arr_CurrentItem : arr_sRecords) {
								if(!Boolean.parseBoolean(arr_CurrentItem.get(7))) {
									TimeRecords.addRow(new Object[] {
											arr_CurrentItem.get(0), arr_CurrentItem.get(1), arr_CurrentItem.get(2), arr_CurrentItem.get(3), arr_CurrentItem.get(4), arr_CurrentItem.get(5),
											dfDuration.format(Double.parseDouble(arr_CurrentItem.get(6)))
									});
								}
							}
							//Clear record array
							arr_sRecords.clear();
							
						} catch (IndexOutOfBoundsException ioobe) {
							//Display errors
							JOptionPane.showMessageDialog(null, "The import failed!", "Import error", JOptionPane.ERROR_MESSAGE);
						} catch (Exception ex) {
							//Display errors
							JOptionPane.showMessageDialog(null, "The import failed!", "Import error", JOptionPane.ERROR_MESSAGE);
						}
						//Allow messages to incomplete entries again
						bImportActive = false;
						
						//Delete buffer row
						RemBuffer();
					}
					
				}
			}
		});
		btnImport.setBounds(512, 23, 126, 26);
		panel_border1.add(btnImport);
		
		/*
		 * Export entries
		 */
		JButton btnExport = new JButton("Export");
		btnExport.setMnemonic('e');
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Create and show file chooser for the import file
				JFileChooser fcExport = new JFileChooser();
				fcExport.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fcExport.setFileFilter(new FileNameExtensionFilter("CSV files \".csv\"", "csv"));
				fcExport.setCurrentDirectory(new File("$HOME"));
				int iStatus = fcExport.showSaveDialog(null);
				
				//Cancel the export if the dialog was canceled
				if(iStatus == JFileChooser.APPROVE_OPTION) {

					//Create file handle for the export file
					String sExportFileName = "";
					try {
						sExportFileName = fcExport.getSelectedFile().getAbsolutePath();
					} catch(Exception exc) {
						//Show error if the path isn't valid
						JOptionPane.showMessageDialog(null, "Please enter a valid save path!", "File error", JOptionPane.ERROR_MESSAGE);
					}
					try {
						//Create stream to write the file
						BufferedWriter bwExport = new BufferedWriter(new FileWriter(sExportFileName));
						
						//String rows together and write line to file
						for (int iRow = 0; iRow < TimeRecords.getRowCount(); iRow++) {
							String sDate = String.valueOf(TimeRecords.getValueAt(iRow, 0)) + ";";
							String sUser = String.valueOf(TimeRecords.getValueAt(iRow, 1)) + ";";
							String sCategory = String.valueOf(TimeRecords.getValueAt(iRow, 2)) + ";";
							String sTask = String.valueOf(TimeRecords.getValueAt(iRow, 3)) + ";";
							String sDuration = String.valueOf(TimeRecords.getValueAt(iRow, 4)) + ";";
							String sStartTime = String.valueOf(TimeRecords.getValueAt(iRow, 5)) + ";";
							String sEndTime = String.valueOf(TimeRecords.getValueAt(iRow, 6));
							String sOutputLine = sDate + sUser + sCategory + sTask + sDuration + sStartTime + sEndTime;
							bwExport.write(sOutputLine);
							bwExport.newLine();
						}
						//Close file handle
						bwExport.close();
					} catch (IOException ioe) {
						//Print stack trace at error
						ioe.printStackTrace();
					}
				}
			}
		});
		btnExport.setBounds(650, 23, 126, 26);
		panel_border1.add(btnExport);
	}
	
	/*
	 * Validation on data changes
	 */
	@Override
	public void tableChanged(TableModelEvent arg0) {
		try {
			//Cancel if no row is selected
			if(tblTimeRecords.getSelectedRow() == -1) {return;}
			
			//Remove the TableModelListener
			TimeRecords.removeTableModelListener(this);
			
			/*
			 * Validate date
			 */
			//Parse to string
			String sDate = (String) TimeRecords.getValueAt(tblTimeRecords.getSelectedRow(), 0);
			
			//Continue only if string isn't empty
			if(!sDate.isEmpty()) {
				try {
					//Try to parse the input to a date, display error if failed
					SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
					sdfDate.setLenient(false);
					@SuppressWarnings("unused")
					Date dateValidateDate = sdfDate.parse(sDate);
				} catch(ParseException pe) {JOptionPane.showMessageDialog(null, "Please enter a valid date (format is MM/DD/YYYY)!", "Invalid input", JOptionPane.ERROR_MESSAGE);}
			} else {
				//Wenn die Datumszeile fehlt und weder eine Löschung noch ein Import aktiv ist, Fehlermeldung anzeigen
				if(bDeletingActive | bImportActive) {} else {
					JOptionPane.showMessageDialog(null, "Please enter a valid date (format is MM/DD/YYYY)!", "Invalid input\"", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			/*
			 * Calculate duration
			 */
			//Cast times to strings
			String sStartTimeNoFormat = (String) TimeRecords.getValueAt(tblTimeRecords.getSelectedRow(), 4);
			String sEndTimeNoFormat = (String) TimeRecords.getValueAt(tblTimeRecords.getSelectedRow(), 5);
			
			//Only continue, if both values are set
			if(!sStartTimeNoFormat.isEmpty() & !sEndTimeNoFormat.isEmpty()) {
				//Split strings at double period
				String[] arr_sStartTime = sStartTimeNoFormat.split(":");
				String[] arr_sEndTime = sEndTimeNoFormat.split(":");
				
				//Parse times to double and calculate the duration
				double dStartTime = Double.parseDouble(arr_sStartTime[0]) + (Double.parseDouble(arr_sStartTime[1]) / 60);
				double dEndTime = Double.parseDouble(arr_sEndTime[0]) + (Double.parseDouble(arr_sEndTime[1]) / 60);
				double dDuration = dEndTime - dStartTime;
				TimeRecords.setValueAt(dfDuration.format(dDuration), tblTimeRecords.getSelectedRow(), 6);
			}
		} catch(Exception e) {}
		//Add TableListener to table again
		TimeRecords.addTableModelListener(this);
	}
	
	/*
	 * Load data from database
	 */
	public void DBLoad() {
		//Create an instance of the JDBC MySQL driver, quit program if it fails
		try {
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null, "Database driver couldn't be loaded!", "Database error", JOptionPane.ERROR_MESSAGE);
		    System.err.println(e);
		    System.exit(-1);
		}
		
		//Create database connection
		try {
		    conn = DriverManager.getConnection(connectURL, user, pw);
		    
		    //Create and run select statement
		    Statement stmt = conn.createStatement();
		    String sLoadQuery = "SELECT * FROM zeiterfassung";
		    ResultSet rsLoadData = stmt.executeQuery(sLoadQuery);
		    
		    //List entries is the table
		    while (rsLoadData.next()) {
		    	//Convert date
		    	String sDateDB = rsLoadData.getString("date");
			    SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd");
			    SimpleDateFormat sdfZE = new SimpleDateFormat("MM/dd/yyyy");
			    Date dateDB = null;
			    try {dateDB = sdfDB.parse(sDateDB);} catch (ParseException e) {}
			    String sDateZE = sdfZE.format(dateDB);
			    
			    //Convert times
			    double dStartTime = Double.parseDouble(rsLoadData.getString("start_time"));
			    double dEndTime = Double.parseDouble(rsLoadData.getString("end_time"));
			    double dStartTimeMinutes = (dStartTime - ((int) dStartTime)) * 60;
			    double dEndTimeMinutes = (dEndTime - ((int) dEndTime)) * 60;
			    String sStartTimeFormat = String.valueOf((int) dStartTime) + ":" + String.format("%02d", ((int) dStartTimeMinutes));
			    String sEndTimeFormat = String.valueOf((int) dEndTime) + ":" + String.format("%02d", ((int) dEndTimeMinutes));
			    
			    //Convert duration
			    String sDurationNoFormat = rsLoadData.getString("duration");
			    double dDurFormat = Double.parseDouble(sDurationNoFormat);
			    String sDurationFormat = dfDuration.format(dDurFormat);
			    
			    //Add row with formatted values to the table
		    	TimeRecords.addRow(new Object[] {sDateZE, rsLoadData.getString("user"), rsLoadData.getString("activity_group"), rsLoadData.getString("activity"),
		    			sStartTimeFormat, sEndTimeFormat, sDurationFormat});
		    }
		    //Colse ResultSet and Connection
		    rsLoadData.close();
		    stmt.close();
		} catch (SQLException e) {
			//Display errors and quit
			JOptionPane.showMessageDialog(null, "SQL error: \n" + e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
		    System.exit(-1);
		} finally {
		    //Verbindung schließen
		    if (conn != null) {
		    	try {
		    		conn.close();
		    	} catch (SQLException e) {
		    		e.printStackTrace();
		    	}
		    }
		}
	}
	
	/*
	 * Save data to database
	 */
	public void DBSave() {
		//Create an instance of the JDBC MySQL driver, quit program if it fails
		try {
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null, "Treiber konnte nicht geladen werden!", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
		    System.err.println(e);
		    System.exit(-1);
		}
		
		//Create database connection
		try {
		    conn = DriverManager.getConnection(connectURL, user, pw);
		    
		    //Create query to truncate table and run it
		    Statement stmt = conn.createStatement();
		    String sSaveQuery = "TRUNCATE zeiterfassung";
		    stmt.executeUpdate(sSaveQuery);
		    
		    //Save data to the database
		    for (int iRow = 0; iRow < TimeRecords.getRowCount(); iRow++) {
				//Convert date
		    	String sDateZE = String.valueOf(TimeRecords.getValueAt(iRow, 0));
			    SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd");
			    SimpleDateFormat sdfZE = new SimpleDateFormat("dd.MM.yyyy");
			    Date dateZE = null;
			    try {dateZE = sdfZE.parse(sDateZE);} catch (ParseException e) {}
			    String sDateDB = "'" + sdfDB.format(dateZE) + "', '";
				
				//Convert times
			    String[] arr_sStartTime, arr_sEndTime;
			    double dStartTime = 0, dEndTime = 0;
			    try {
			    	arr_sStartTime = String.valueOf(TimeRecords.getValueAt(iRow, 4)).split(":");
			    	arr_sEndTime = String.valueOf(TimeRecords.getValueAt(iRow, 5)).split(":");
					dStartTime = Double.parseDouble(arr_sStartTime[0]) + (Double.parseDouble(arr_sStartTime[1]) / 60);
					dEndTime = Double.parseDouble(arr_sEndTime[0]) + (Double.parseDouble(arr_sEndTime[1]) / 60);
			    } catch(NumberFormatException nfe) {}
				
				//Create string with input values
				String sUser = String.valueOf(TimeRecords.getValueAt(iRow, 1)) + "', '";
				String sCategory = String.valueOf(TimeRecords.getValueAt(iRow, 2)) + "', '";
				String sTask = String.valueOf(TimeRecords.getValueAt(iRow, 3)) + "', '";
				String sStartTime = dStartTime + "', '";
				String sEndTime = dEndTime + "', '";
				String sDuration = String.valueOf(TimeRecords.getValueAt(iRow, 6)) + "'";
				String sOutputLine = sDateDB + sUser + sCategory + sTask + sStartTime + sEndTime + sDuration;
				
				//Create insert query and run it
				String sSaveData = "INSERT INTO zeiterfassung(`date`, `user`, `activity_group`, `activity`, `start_time`, `end_time`, `duration`) VALUES (" + sOutputLine + ")";
				stmt.executeUpdate(sSaveData);
		    }
		    stmt.close();
		} catch (SQLException e) {
			//Display errors and quit
			JOptionPane.showMessageDialog(null, "SQL error: \n" + e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
		    System.exit(-1);
		} finally {
		    //Close connection
		    if (conn != null) {
		    	try {
		    		conn.close();
		    	} catch (SQLException e) {
		    		e.printStackTrace();
		    	}
		    }
		}
	}
	
	/*
	 * Create buffer row
	 */
	public void SetBuffer() {
		if(tblTimeRecords.getRowCount() == 0) {
			TimeRecords.addRow(new Object[] {});
		}
	}
	/*
	 * Delete Buffer row
	 */
	public void RemBuffer() {
		if(tblTimeRecords.getValueAt(0, 0) == null) {
			TimeRecords.removeRow(0);
		}
	}
}