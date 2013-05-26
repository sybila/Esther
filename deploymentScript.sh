echo "Please enter the mysql database username:"
read mysql_user
echo "Please enter the mysql database user password:"
read mysql_pass

sed -i -e "s#jdbc\.username=.*#jdbc\.username=$mysql_user#" ./Sources/Esther/src/main/resources/Properties/Database.properties
sed -i -e "s#jdbc\.password=.*#jdbc\.password=$mysql_pass#" ./Sources/Esther/src/main/resources/Properties/Database.properties

mysql -u$mysql_user -p$mysql_pass -e "CREATE DATABADE IF NOT EXISTS EsherDB;"
mysql -u$mysql_user -p$mysql_pass EstherDB < createTables.sql

echo "Please specify the path to Parsybone binary:"
read parsybone_loc

sed -i -e "s#parsybone_location=.*#parsybone_location=$parsybone_loc#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

echo "Select the path to data storage location:"
read data_loc

mkdir $data_loc
chmod 777 $data_loc

sed -i -e "s#data_location=.*#data_location=$data_loc#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

echo "Maximum allowed storage space per user [in bytes]:"
read max_storage

sed -i -e "s#allowed_storage_space=.*#allowed_storage_space=$max_storage#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

echo "Maximum allowed parallel tasks per user"
read max_tasks

sed -i -e "s#allowed_parallel_tasks=.*#allowed_parallel_tasks=$max_tasks#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

mvn package -f Sources/EstherHeart/pom.xml
mvn install:install-file -f Sources/EstherHeart/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-heart \
-Dversion=1.0 -Dfile=target/esther-heart-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/SQLiteManager/pom.xml
mvn install:install-file -f Sources/SQLiteManager/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-sqlite \
-Dversion=1.0 -Dfile=target/esther-sqlite-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ModelEditorWidget/pom.xml
mvn install:install-file -f Sources/ModelEditorWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-model_editor_widget \
-Dversion=1.0 -Dfile=target/esther-model_editor_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ParsyboneWidget/pom.xml
mvn install:install-file -f Sources/ParsyboneWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-parsybone_widget \
-Dversion=1.0 -Dfile=target/esther-parsybone_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ParameterViewWidget/pom.xml
mvn install:install-file -f Sources/ParameterViewWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-parameter_view_widget \
-Dversion=1.0 -Dfile=target/esther-parameter_view_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/BehaviourMapWidget/pom.xml
mvn install:install-file -f Sources/BehaviourMapWidget/pom.xml \
-DgroupId=mu.fi.sybila -DartifactId=esther-behaviour_map_widget \
-Dversion=1.0 -Dfile=target/esther-behaviour_map_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn tomcat:deploy -f Sources/Esther/pom.xml
