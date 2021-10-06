echo "======================================="
echo "      packaging brchain with mvn       "
echo "======================================="


./mvnw package -Dmaven.test.skip=true

echo "======================================="
echo "     start db and brchain container    "
echo "======================================="


docker-compose up -d
