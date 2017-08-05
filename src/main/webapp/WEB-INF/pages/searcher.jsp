<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>文章搜索</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/bootstrap.min.css"/>
</head>

<body>
<div class="container-fluid">
  
  <div class="row">
  	<div class="col-sm-12">
    	<h2>文章搜索 </h2>
        <form class="form-inline" action="${pageContext.request.contextPath}/article/search">
          <div class="form-group">
            <input type="text" name="keyword" class="form-control" placeholder="输入关键字">
          </div>
          <button type="submit" class="btn btn-default">Search</button>
        </form>
    </div>
  </div>
  <div class="row">
  	<div class="col-sm-12">
        <c:forEach items="${articles }" var="article">
        	<div class="media">
	          <div class="media-body">
	            <h4 class="media-heading"><a href="${pageContext.request.contextPath}/article/${article.id}">${article.title }</a></h4>
	            <p>
	            	${article.details }
	            </p>
	          </div>
	        </div>
        </c:forEach>
    </div>
  </div>
</div>


<script src="${pageContext.request.contextPath}/static/js/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/static/js/bootstrap.min.js"></script>
</body>
</html>
