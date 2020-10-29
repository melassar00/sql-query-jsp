<!doctype html>
<%--   
 /* Name: Muhamad Elassar
 * Course: CNT 4714 Spring 2020
 * Project Four: Developing A Three-Tier Distributed Web-Based Application
 *Date: April 20, 2020
*/
--%>
<%
	String textBox = (String) session.getAttribute("textBox");
String result = (String) session.getAttribute("result");
if (result == null) {
	result = " ";
}
if (textBox == null) {
	textBox = " ";
}
%>



<html lang="en">

<body>
	<div>
		<h1>Welcome to the Spring 2020 Project 4 Enterprise System</h1>
		<h2>A Remote Database Management System</h2>
		<div>You are connected to the Project 4 Database.</div>
		<div>Please enter any valid sql query or update statement.</div>
		<div>If no query/update command is given the Execute button will
			display all supplier information in the database.</div>
		<div>All execution results will appear below</div>
		<form action="/Assignment4/SQLQueryServlet" method="post"
			style="margin-top: 15px;">
			<div>
				<div>
					<textarea name="textBox" id="textBox" rows="8" cols="50"><%=textBox%></textarea>
				</div>
			</div>
			<button style="margin-bottom: 15px;" type="submit">Execute
				Command</button>
			<button style="margin-bottom: 15px;" type="reset" value="Reset">Clear
				Form</button>
		</form>
	</div>


	<div>
		<%=result%>
	</div>

</body>

</html>