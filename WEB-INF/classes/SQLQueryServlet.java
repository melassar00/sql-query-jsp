/* Name: Muhamad Elassar
 * Course: CNT 4714 Spring 2020
 * Project Four: Developing A Three-Tier Distributed Web-Based Application
 *Date: April 20, 2020
*/

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLQueryServlet extends HttpServlet {
//	initialize the connection and statement
	private Connection SQLconnection;
	private Statement SQLstatement;

//	initialize database
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			Class.forName(config.getInitParameter("databaseDriver"));
//			enter credentials
			SQLconnection = DriverManager.getConnection(config.getInitParameter("databaseName"),
					config.getInitParameter("username"), config.getInitParameter("password"));
			SQLstatement = SQLconnection.createStatement(); // output statement
		}

		catch (Exception e) {
			e.printStackTrace();
			throw new UnavailableException(e.getMessage());
		}

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String textBox = request.getParameter("textBox");
		String queryResult = null;

//		perform sql queries
		if (textBox.toLowerCase().contains("select")) {

			try {
				queryResult = doSelectQuery(textBox.toLowerCase());
			} catch (SQLException e) {
				queryResult = "<span>" + e.getMessage() + "</span>";

				e.printStackTrace();
			}
		} else {
			try {
				queryResult = doUpdateQuery(textBox.toLowerCase());
			} catch (SQLException e) {
				queryResult = "<span>" + e.getMessage() + "</span>";

				e.printStackTrace();
			}
		}

//		pass result to http session and request from jsp
		HttpSession session = request.getSession();
		session.setAttribute("result", queryResult);
		session.setAttribute("textBox", textBox);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		call doGet with the request and response
		doGet(request, response);
	}

	public String doSelectQuery(String textBox) throws SQLException {
		String queryResult;
		ResultSet table = SQLstatement.executeQuery(textBox);

//		prepare html for the query
		ResultSetMetaData metaData = table.getMetaData();
		int numCols = metaData.getColumnCount();

//		prepare table
		String tableOpenTags = "<div><div><div><table>";
		String tableColumnTags = "<thead><tr>";
		for (int i = 1; i <= numCols; i++) {
			tableColumnTags += "<th scope='col'>" + metaData.getColumnName(i) + "</th>";
		}

		tableColumnTags += "</tr></thead>";

//		prepare body
		String tableBodyTag = "<tbody>";
		while (table.next()) {
			tableBodyTag += "<tr>";
			for (int i = 1; i <= numCols; i++) {
				if (i == 1)
					tableBodyTag += "<td scope'row'>" + table.getString(i) + "</th>";
				else
					tableBodyTag += "<td>" + table.getString(i) + "</th>";
			}
			tableBodyTag += "</tr>";
		}

		tableBodyTag += "</tbody>";

//		close the html tags
		String tableClosingTags = "</table></div></div></div>";
		queryResult = tableOpenTags + tableColumnTags + tableBodyTag + tableClosingTags;

		return queryResult;
	}

	private String doUpdateQuery(String textBoxLowerCase) throws SQLException {
		String queryResult = null;
		int numUpdatedRows = 0;

//		check quantity
		ResultSet quantityPreCheck = SQLstatement.executeQuery("select COUNT(*) from shipments where quantity >= 100");
		quantityPreCheck.next();
		int numBigPreShipments = quantityPreCheck.getInt(1);

//		execute updates
		SQLstatement.executeUpdate("create table shipmentsBeforeUpdate like shipments");
		SQLstatement.executeUpdate("insert into shipmentsBeforeUpdate select * from shipments");

		numUpdatedRows = SQLstatement.executeUpdate(textBoxLowerCase);

//		add html
		queryResult = "<div> The statement executed succesfully.</div><div>" + numUpdatedRows
				+ " row(s) affected</div>";

//		check quantity again
		ResultSet quantityPostCheck = SQLstatement.executeQuery("select COUNT(*) from shipments where quantity >= 100");
		quantityPostCheck.next();
		int numBigPostShipments = quantityPostCheck.getInt(1);

//		add html
		queryResult += "<div>" + numBigPreShipments + " < " + numBigPostShipments + "</div>";

		if (numBigPreShipments < numBigPostShipments) {
			int numberOfRowsAffectedAfterIncrementBy5 = SQLstatement.executeUpdate(
					"update suppliers set status = status + 5 where snum in ( select distinct snum from shipments left join shipmentsBeforeUpdate using (snum, pnum, jnum, quantity) where shipmentsBeforeUpdate.snum is null)");
			queryResult += "<div>Business Logic Detected! - Updating Supplier Status</div>";
			queryResult += "<div>Business Logic Updated " + numberOfRowsAffectedAfterIncrementBy5
					+ " Supplier(s) status marks</div>";
		}

//		execute final update
		SQLstatement.executeUpdate("drop table shipmentsBeforeUpdate");

		return queryResult;
	}

}
