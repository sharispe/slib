#!/usr/bin/python
print "Generating HTML"



html = ""
html += "<html>"
html += "<head>"
html += "</head>"

html += "<body>"
html += "<h1>Results</h1>"
html += "Semantic Measures comparison"
html += "<a href='test.pdf'> details </a>"
html += "<center>"
html += "<table border='1'>"

html += "<tr><th> Name </th><th> Score </th></tr>"
file = open("output.txt",'r')
for l in file:
	l = l.strip()
	data = l.split("\t")
	html += "<tr><td>"
	html += "</td><td>".join(data)
	html += "</td></tr>"
file.close()
html += "</table>"
html += "</center>"

html += "</body>"


html += "</html>"

print html
file = open("output.html",'w')
file.write(html)
file.close()
