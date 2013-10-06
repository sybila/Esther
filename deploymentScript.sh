echo "Please enter the mysql database username:"
read mysql_user
echo "Please enter the mysql database user password:"
read mysql_pass

sed -i -e "s#jdbc\.username=.*#jdbc\.username=$mysql_user#" ./Sources/Esther/src/main/resources/Properties/Database.properties
sed -i -e "s#jdbc\.password=.*#jdbc\.password=$mysql_pass#" ./Sources/Esther/src/main/resources/Properties/Database.properties

mysql -u$mysql_user -p$mysql_pass -e "CREATE DATABASE IF NOT EXISTS EstherDB;"
mysql -u$mysql_user -p$mysql_pass EstherDB < createTables.sql

echo "Please specify the path to Parsybone binary:"
read parsybone_loc

sed -i -e "s#parsybone_location=.*#parsybone_location=$parsybone_loc#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

echo "Select the path to data storage location:"
read data_loc

mkdir $data_loc
chmod 777 $data_loc

sed -i -e "s#data_location=.*#data_location=$data_loc#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

repo_loc=$data_loc
repo_loc+="mvn_repo/"

mkdir $repo_loc

sed -i -e "s#<url>file:.*</url>#<url>file:$repo_loc</url>#" ./Sources/Esther/pom.xml
sed -i -e "s#<url>file:.*</url>#<url>file:$repo_loc</url>#" ./Sources/EstherHeart/pom.xml
sed -i -e "s#<url>file:.*</url>#<url>file:$repo_loc</url>#" ./Sources/ModelEditorWidget/pom.xml
sed -i -e "s#<url>file:.*</url>#<url>file:$repo_loc</url>#" ./Sources/ParsyboneWidget/pom.xml
sed -i -e "s#<url>file:.*</url>#<url>file:$repo_loc</url>#" ./Sources/ParameterViewWidget/pom.xml
sed -i -e "s#<url>file:.*</url>#<url>file:$repo_loc</url>#" ./Sources/BehaviourMapWidget/pom.xml
sed -i -e "s#<url>file:.*</url>#<url>file:$repo_loc</url>#" ./Sources/SQLiteManager/pom.xml

echo "Maximum allowed storage space per user [in bytes]:"
read max_storage

sed -i -e "s#allowed_storage_space=.*#allowed_storage_space=$max_storage#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

echo "Maximum allowed parallel tasks per user:"
read max_tasks

sed -i -e "s#allowed_parallel_tasks=.*#allowed_parallel_tasks=$max_tasks#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

echo "Enter your reCAPTCHA public key:"
read captcha_public

sed -i -e "s#captcha_public_key=.*#captcha_public_key=$captcha_public#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

echo "Enter your reCAPTCHA private key:"
read captcha_private

sed -i -e "s#captcha_private_key=.*#captcha_private_key=$captcha_private#" ./Sources/Esther/src/main/resources/Properties/Esther.properties

mvn package -f Sources/EstherHeart/pom.xml
mvn deploy:deploy-file -f Sources/EstherHeart/pom.xml \
-Durl=file://$repo_loc \
-DgroupId=mu.fi.sybila -DartifactId=esther-heart \
-Dversion=1.0 -Dfile=target/esther-heart-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/SQLiteManager/pom.xml
mvn deploy:deploy-file -f Sources/SQLiteManager/pom.xml \
-Durl=file://$repo_loc \
-DgroupId=mu.fi.sybila -DartifactId=esther-sqlite \
-Dversion=1.0 -Dfile=target/esther-sqlite-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ModelEditorWidget/pom.xml
mvn deploy:deploy-file -f Sources/ModelEditorWidget/pom.xml \
-Durl=file://$repo_loc \
-DgroupId=mu.fi.sybila -DartifactId=esther-model_editor_widget \
-Dversion=1.0 -Dfile=target/esther-model_editor_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ParsyboneWidget/pom.xml
mvn deploy:deploy-file -f Sources/ParsyboneWidget/pom.xml \
-Durl=file://$repo_loc \
-DgroupId=mu.fi.sybila -DartifactId=esther-parsybone_widget \
-Dversion=1.0 -Dfile=target/esther-parsybone_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/ParameterViewWidget/pom.xml
mvn deploy:deploy-file -f Sources/ParameterViewWidget/pom.xml \
-Durl=file://$repo_loc \
-DgroupId=mu.fi.sybila -DartifactId=esther-parameter_view_widget \
-Dversion=1.0 -Dfile=target/esther-parameter_view_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn package -f Sources/BehaviourMapWidget/pom.xml
mvn deploy:deploy-file -f Sources/BehaviourMapWidget/pom.xml \
-Durl=file://$repo_loc \
-DgroupId=mu.fi.sybila -DartifactId=esther-behaviour_map_widget \
-Dversion=1.0 -Dfile=target/esther-behaviour_map_widget-1.0.jar \
-Dpackaging=jar -DgeneratePom=true

mvn tomcat:deploy -f Sources/Esther/pom.xml
