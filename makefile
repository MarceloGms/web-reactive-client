# run all reqs
run:
	@mvn exec:java -Dexec.args="req1.txt req2.txt req3.txt req4.txt req5.txt req6.txt req7.txt req8.txt req9.txt req10.txt"

# run specific reqs
run1:
	@mvn exec:java -Dexec.args="$(ARGS)"