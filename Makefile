JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Movie.java \
	Cluster.java \
	Attribute.java \
	MovieHandler.java \
	MovieRankHandler.java \
	MovieDatabase.java \
	MovieServer.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
