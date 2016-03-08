all: LaSer.class VoterCli.class VfSer.class

LaSer.class: LaSer.java
	javac LaSer.java

VoterCli.class: VoterCli.java
	javac VoterCli.java

VfSer.class: VfSer.java
	javac VfSer.java

clean:
	rm -f  *~ *.class *java.save.*
