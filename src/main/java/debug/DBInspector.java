package debug;

import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Small utility to inspect `resultat` rows and linked users.
 * Run with: mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=debug.DBInspector
 */
public class DBInspector {
	public static void main(String[] args) {
		System.out.println("DBInspector starting...");
		Connection conn = MyDataBase.getInstance().getConnection();
		if (conn == null) {
			System.out.println("No DB connection available. Aborting.");
			return;
		}

		try {
			// 1) Inspect resultat table columns (helpful if schema differs)
			System.out.println("\n== Columns in resultat table ==");
			try (PreparedStatement colStmt = conn.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resultat'")) {
				try (ResultSet cols = colStmt.executeQuery()) {
					int i = 0;
					while (cols.next()) {
						i++;
						System.out.printf("%d) %s (%s)\n", i, cols.getString("COLUMN_NAME"), cols.getString("DATA_TYPE"));
					}
					if (i == 0) System.out.println("(resultat table not found or no columns)");
				}
			}

			// 2) Try to select a single row and print its metadata and values
			System.out.println("\n== Sample row from resultat (metadata & values) ==");
			try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM resultat LIMIT 1")) {
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						java.sql.ResultSetMetaData md = rs.getMetaData();
						int cols = md.getColumnCount();
						for (int c = 1; c <= cols; c++) {
							System.out.printf("%d) %s (%s) = %s\n", c, md.getColumnLabel(c), md.getColumnTypeName(c), rs.getObject(c));
						}
					} else {
						System.out.println("(no rows in resultat)");
					}
				}
			}

			// 2) If a score value is provided as arg, filter by that score
			if (args.length > 0) {
				String arg = args[0];
				// If argument looks like eval:<id>, list resultat rows for that evaluation id
				if (arg.startsWith("eval:")) {
					String evalStr = arg.substring(5);
					try {
						int evalId = Integer.parseInt(evalStr);
						System.out.println("\n== Rows for evaluation_id = " + evalId + " ==");
						try (PreparedStatement psEval = conn.prepareStatement("SELECT r.id, r.student_id, r.evaluation_id, r.score, u.nom, u.prenom FROM resultat r LEFT JOIN utilisateurs u ON r.student_id = u.id WHERE r.evaluation_id = ? ORDER BY r.id DESC")) {
							psEval.setInt(1, evalId);
							try (ResultSet rsEval = psEval.executeQuery()) {
								int k = 0;
								while (rsEval.next()) {
									k++;
									System.out.printf("id=%d student_id=%d eval_id=%d score=%s user=%s %s\n",
											rsEval.getInt("id"), rsEval.getInt("student_id"), rsEval.getInt("evaluation_id"), rsEval.getObject("score"), rsEval.getString("prenom"), rsEval.getString("nom")
									);
								}
								if (k == 0) System.out.println("(no rows for that evaluation)");
							}
						}
					} catch (NumberFormatException nfe) {
						System.out.println("Invalid evaluation id provided: " + evalStr);
					}
					// done
				} else {
					try {
						Float scoreFilter = Float.parseFloat(arg);
						System.out.println("\n== Searching for resultat rows with score = " + scoreFilter + " ==");
						try (PreparedStatement ps2 = conn.prepareStatement("SELECT r.id, r.student_id, r.evaluation_id, r.score, u.nom, u.prenom FROM resultat r LEFT JOIN utilisateurs u ON r.student_id = u.id WHERE r.score = ? ORDER BY r.id DESC")) {
							ps2.setFloat(1, scoreFilter);
							try (ResultSet rs2 = ps2.executeQuery()) {
								int c = 0;
								while (rs2.next()) {
									c++;
									System.out.printf("id=%d student_id=%d eval_id=%d score=%s user=%s %s\n",
											rs2.getInt("id"), rs2.getInt("student_id"), rs2.getInt("evaluation_id"), rs2.getObject("score"), rs2.getString("prenom"), rs2.getString("nom")
									);
								}
								if (c == 0) System.out.println("(no rows with that score)");
							}
						}
					} catch (NumberFormatException nfe) {
						// Not a numeric score; try lookup by evaluation title instead
						String title = arg;
						System.out.println("\n== Lookup evaluation by title LIKE '%"+title+"%' ==");
						try (PreparedStatement ps3 = conn.prepareStatement("SELECT id, titre, note_max FROM evaluation WHERE titre LIKE ? LIMIT 10")) {
							ps3.setString(1, "%"+title+"%");
							try (ResultSet rs3 = ps3.executeQuery()) {
								while (rs3.next()) {
									System.out.printf("evaluation id=%d titre=%s note_max=%s\n", rs3.getInt("id"), rs3.getString("titre"), rs3.getObject("note_max"));
								}
							}
						}
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("SQL error: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("DBInspector finished.");
	}
}




