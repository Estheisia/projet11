# Création d'un service de passation de QCM par Barneaud Gabriel et Huyghe Rémi

## Mode développement
Pour packager l'application et démarrer le serveur, on utilise la commande suivante :

    mvn clean package payara-micro:start

Pour lancer la page principale de l'application allez sur :

    http://localhost:8080/projetqcm

## Requêtes implémentées
Faire attention lors de leur utilisation à deux points :
- Les ID doivent correspondre à ceux dans la BD, pour y accèder : 
    
    http://localhost:8080/projetqcm/dbconsole

- Les tokens ont une grande durée de vie, il ne devrait pas expirer, mais il faut penser à selectionner le bon.

1. GET /qcm
1. GET /qcm/{idExam}/inscrits
1. PUT /qcm/{idExam}/inscrits
1. PATCH /qcm/{idExam}/inscrits
1. DELETE /qcm/{idExam}/inscrits
1. DELETE /qcm/{idExam}/inscrits/{idPersonne}
1. GET /qcmencour
1. GET /qcmencours/{id}