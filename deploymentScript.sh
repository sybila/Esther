mvn package -f Sources/EstherHeart/pom.xml
mvn install:install-file -f Sources/EstherHeart/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-heart \
-Dversion=1.0-SNAPSHOT -Dfile=target/esther-heart-1.0-SNAPSHOT.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/SQLiteManager/pom.xml
mvn install:install-file -f Sources/SQLiteManager/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-sqlite \
-Dversion=1.0-SNAPSHOT -Dfile=target/esther-sqlite-1.0-SNAPSHOT.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ModelEditorWidget/pom.xml
mvn install:install-file -f Sources/ModelEditorWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-model_editor_widget \
-Dversion=1.0-SNAPSHOT -Dfile=target/esther-model_editor_widget-1.0-SNAPSHOT.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ParsyboneWidget/pom.xml
mvn install:install-file -f Sources/ParsyboneWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-parsybone_widget \
-Dversion=1.0-SNAPSHOT -Dfile=target/esther-parsybone_widget-1.0-SNAPSHOT.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ParameterViewWidget/pom.xml
mvn install:install-file -f Sources/ParameterViewWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-parameter_view_widget \
-Dversion=1.0-SNAPSHOT -Dfile=target/esther-parameter_view_widget-1.0-SNAPSHOT.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/BehaviourMapWidget/pom.xml
mvn install:install-file -f Sources/BehaviourMapWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-behaviour_map_widget \
-Dversion=1.0-SNAPSHOT -Dfile=target/esther-behaviour_map_widget-1.0-SNAPSHOT.jar \
-Dpackaging=jar -DgeneratePom=true

mvn tomcat:deploy -f Sources/Esther/pom.xml
