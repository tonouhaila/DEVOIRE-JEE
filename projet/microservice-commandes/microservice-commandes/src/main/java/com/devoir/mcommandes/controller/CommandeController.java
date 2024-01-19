package com.devoir.mcommandes.controller;

import com.devoir.mcommandes.configurations.ApplicationPropertiesConfiguration;
import com.devoir.mcommandes.dao.CommandeRepository;
import com.devoir.mcommandes.model.Commande;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class CommandeController implements HealthIndicator {
    @Autowired
    CommandeRepository commandeRepository;

    @Autowired
    ApplicationPropertiesConfiguration appProperties;

    // Affiche la liste de toutes les commandes des 10 / 20 derniers jours
    @GetMapping(value = "/Commandes")
    public List<Commande> listeDesCommandes() throws Exception {
        System.out.println(" ********* CommandeController Liste des commandes ");
        List<Commande> commandes = commandeRepository.findAll();
        if (commandes.isEmpty())
            throw new Exception("Aucune commande actuellement");

        LocalDate dateLimite = LocalDate.now().minusDays(appProperties.getCommandesLast());

        // Filtrer les commandes reçues au cours des 10 derniers jours
        List<Commande> commandesRecentes = commandes.stream()
                .filter(commande -> commande.getDate().isAfter(dateLimite))
                .collect(Collectors.toList());

        return commandesRecentes;

    }
    // Récuperer une commande par son id
    @GetMapping(value = "/Commandes/{id}")
    public Optional<Commande> recupererUneCommande(@PathVariable int id)throws Exception {
        System.out.println(" ********* CommandeController recupererUneCommande(@PathVariable int id) ");
        Optional<Commande> commande = commandeRepository.findById(id);
        if (!commande.isPresent())
            throw new Exception("La commande correspondante à l'id " + id + " n'existe pas");
        return commande;
    }
    @PostMapping(value = "/add")
    public ResponseEntity<Commande> createCommande(@RequestBody Commande newCommande) {
        System.out.println(" ********* CommandeController createCommande ");
        Commande createdCommande = commandeRepository.save(newCommande);
        return new ResponseEntity<>(createdCommande, HttpStatus.CREATED);
    }

    // Update an existing command
    @PutMapping(value = "/Commandes/{id}")
    public ResponseEntity<Commande> updateCommande(@PathVariable int id, @RequestBody Commande updatedCommande) throws Exception {
        System.out.println(" ********* CommandeController updateCommande ");
        Optional<Commande> existingCommande = commandeRepository.findById(id);
        if (existingCommande.isPresent()) {
            updatedCommande.setId(id); // Ensure the ID is set for the update
            Commande savedCommande = commandeRepository.save(updatedCommande);
            return new ResponseEntity<>(savedCommande, HttpStatus.OK);
        } else {
            throw new Exception("La commande correspondante à l'id " + id + " n'existe pas");
        }
    }

    // Delete a command by its ID
    @DeleteMapping(value = "/Commandes/{id}")
    public ResponseEntity<Void> deleteCommande(@PathVariable int id) throws Exception {
        System.out.println(" ********* CommandeController deleteCommande ");
        Optional<Commande> existingCommande = commandeRepository.findById(id);
        if (existingCommande.isPresent()) {
            commandeRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new Exception("La commande correspondante à l'id " + id + " n'existe pas");
        }
    }

    @Override
    public Health health() {
        System.out.println("****** Actuator : CommandeController health() ");
        List<Commande> commandes = commandeRepository.findAll();
        if (commandes.isEmpty()) {
            return Health.down().build();
        }
        return Health.up().build();
    }
}