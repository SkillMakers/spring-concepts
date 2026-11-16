#!/bin/sh
# Compile, teste, joue chaque scenario de la video et ecrit les sorties dans preuves/.
# A lancer par : docker compose run --rm demo   (voir le README)
set -eu
cd "$(dirname "$0")"
P=preuves
mkdir -p "$P"

{
  echo "\$ java -version"; java -version 2>&1
  echo "\$ mvn -version | head -1"; mvn -version | head -1
} > "$P/versions-outils.txt"

mvn -q -B clean verify > /tmp/mvn.log 2>&1 || { cat /tmp/mvn.log; exit 1; }
{
  echo "\$ mvn verify   (un test par ligne : classe, methode, duree)"
  cat target/surefire-reports/TEST-*.xml | grep -o '<testcase name="[^"]*" classname="[^"]*" time="[^"]*"' \
    | sed 's/<testcase name="\([^"]*\)" classname="boutique\.\([^"]*\)" time="\([^"]*\)"/ok   \2   \1   (\3 s)/' | sort
  echo "Total : $(cat target/surefire-reports/TEST-*.xml | grep -c '<testcase ') tests, $(cat target/surefire-reports/TEST-*.xml | grep -c -E '<(failure|error)') en echec"
} > "$P/tests.txt"

JAR=target/spring-concepts-1.0.jar
SANS_WEB="--spring.main.web-application-type=none --spring.main.banner-mode=off"

# --- Les scenarios (beans, proxy, transactions, le piege) : sortie de la classe Demo, sans les journaux
{
  echo "\$ java -jar $JAR --demo=true"
  java -jar $JAR $SANS_WEB --demo=true --logging.level.root=OFF
} > "$P/demo.txt"

# --- Les deux erreurs de demarrage : on garde le rapport de Spring Boot, a partir de « APPLICATION FAILED TO START »
demarrage() {  # $1 = propriete
  echo "\$ java -jar $JAR --$1"
  set +e
  java -jar $JAR $SANS_WEB "--$1" > /tmp/demarrage.log 2>&1
  code=$?
  set -e
  sed -n '/APPLICATION FAILED TO START/,$p' /tmp/demarrage.log | sed '/^$/N;/^\n$/D'
  echo "(code de sortie : $code)"
}
demarrage paiement.double=true > "$P/demarrage-deux-beans.txt"
demarrage paiement.actif=false > "$P/demarrage-aucun-bean.txt"

# --- Entite ou DTO : l'API web, interrogee avec curl
java -jar $JAR > /tmp/api.log 2>&1 &
PID=$!
trap 'kill $PID 2>/dev/null || true' EXIT
i=0
# Tomcat repond avant que Donnees ait fini de remplir la base : on attend un vrai 200
until [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/clients/1)" = 200 ]; do
  i=$((i+1)); [ $i -lt 120 ] || { echo "API muette"; cat /tmp/api.log; exit 1; }; sleep 0.5
done
appel() {
  echo "\$ curl $1"
  rm -f /tmp/corps
  code=$(curl -s -o /tmp/corps -w '%{http_code}' "$1" || true)
  # 000 = pas de reponse du serveur : on s'arrete plutot que d'ecrire une fausse preuve
  [ "$code" != 000 ] || { echo "pas de reponse pour $1"; exit 1; }
  echo "HTTP $code"
  # un corps tres long est coupe : on en garde le debut et sa taille
  taille=$(wc -c < /tmp/corps | tr -d ' ')
  if [ "$taille" -gt 600 ]; then
    head -c 600 /tmp/corps; echo " [...]"
    echo "($taille octets en tout ; les 60 derniers : $(tail -c 60 /tmp/corps))"
  else cat /tmp/corps; echo; fi
  echo
}
{
  appel http://localhost:8080/clients/2/entite
  appel http://localhost:8080/clients/1/entite
  appel http://localhost:8080/clients/1
  appel http://localhost:8080/clients/2
  appel http://localhost:8080/clients/99
} > "$P/dto.txt"
grep -E "open-in-view|nesting depth" /tmp/api.log | cut -c1-400 > "$P/api-journal.txt" || true

# --- La meme entite avec spring.jpa.open-in-view=false (ce que recommande l'avertissement)
java -jar $JAR --server.port=8081 --spring.jpa.open-in-view=false > /tmp/api2.log 2>&1 &
PID2=$!
trap 'kill $PID $PID2 2>/dev/null || true' EXIT
i=0
until [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/clients/1)" = 200 ]; do
  i=$((i+1)); [ $i -lt 120 ] || { echo "API 8081 muette"; cat /tmp/api2.log; exit 1; }; sleep 0.5
done
{
  appel http://localhost:8081/clients/1/entite
  echo "(journal du serveur, premiere ligne d'erreur :)"
  grep -m1 -E "lazily initialize|LazyInitialization" /tmp/api2.log | cut -c1-400
} > "$P/dto-sans-open-in-view.txt"

echo "Preuves ecrites dans $P/ :"; ls "$P"
