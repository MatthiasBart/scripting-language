# Interpreter Design

Lexer und Parser sind denke ich ein wesentlicher Bestandteil der uns gute Anhaltspunkte gibt was wir brauchen und ich denke ein Minimum Requirement. Darum herum können wir nur mehr ganz wenig oder ganz viel hinzufügen, je nachdem wie wir planen. Für den Lexer sollten wir anhand der Language Spec Token (also Klassen/Kategorien für Characterfolgen) definieren, welche dann auch vom Parser entsprechend genutzt werden sollten. Auch der Output des Parsers (AST) sollte im besten Fall ohne weitere Transformationen für Semantische Analyse und Evaluation herhalten. Dh. unsere Arbeit sollte sich auf die Tokenliste und den AST beschränken. Die Tokenliste kann denke ich eine einfache Liste sein, welche Datenstruktur für den AST am besten ist können wir noch recherchieren. Dazu wäre es denke ich gut, wenn jeder von uns genau EINE Resource findet die uns bei dem Projekt gut weiterhilft und die Anderen lesen das. So haben wir ähnliche Ideen wie wir den Interpreter umsetzten können. 

Meine Resource wird man auf die Schnelle nicht als Ganze lesen können aber sie ist sehr umfangreich und könnte uns als gute Grundlage dienen. Ich hatte leider noch keine Zeit sie mir im Detail durchzusehen.

- https://edu.anarcho-copy.org/Programming%20Languages/Go/writing%20an%20INTERPRETER%20in%20go.pdf

Die ersten Schritte wären denke ich: 

- Die Sprache zu definieren (übernehmen wir seine oder ändern wir diese ab, machen wir sie ganz neu) 
- Die Architektur zu definieren (reicht uns Lexer, Parser für jetzt? ich denke schon für den Anfang)
- Die Datenstrukturen für Token und AST zu definieren, was sich Teilweise aus der Sprach Definition ergibt
