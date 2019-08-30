<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="depform" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
	<link href="webjars/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
	<spring:url value="/resources/css/style.css" var="mainCss" />
	<spring:url value="/resources/images/header.jpg" var="headerImg" />
	<spring:url value="/resources/images/footer.jpg" var="footerImg" />
	<meta charset="UTF-8">
	<title>Deposit</title>
	<link rel="stylesheet" href="${mainCss}" />
</head>

<body>
	<div class="container">
		<%@ include file="common/header-common.jspf" %>
		<section>
			<h1>DEPOSIT Into Account</h1>
			<h3>Welcome ${fullname}!</h3>
			<depform:form modelAttribute="amount" action="/deposit-bank" method="POST">
				<p>Select Account:<br />
					<input type="radio" name="account" value="chk" checked> Checking (${acctchk})<br />
					<input type="radio" name="account" value="sav"> Saving (${acctsav})<br />
					<input type="radio" name="account" value="loan"> Payment to Cash Advance (${acctloan})<br />
				</p>
				<p><depform:label path="amount">Amount to deposit:</depform:label>
				<br />
				<depform:input type="text" path="amount"/>
				<depform:errors path="amount" cssClass="error" /></p>				
				<p class="error">${errormessage}</p>
				<p>
					<input class="btn btn-success" type="submit" value="Submit Deposit">
					<a class="btn btn-primary" href="/dashboard">Cancel and Return to Dashboard</a>
				</p>
			</depform:form>
		</section>
		<%@ include file="common/footer-common.jspf" %>
	</div>
	<script src="webjars/jquery/1.9.1/jquery.min.js"></script>
    <script src="webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
</body>

</html>