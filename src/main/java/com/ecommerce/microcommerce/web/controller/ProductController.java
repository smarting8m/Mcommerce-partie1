package com.ecommerce.microcommerce.web.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "API pour les opérations CRUD sur les produits.")
@RestController
public class ProductController {

	@Autowired
	private ProductDao productDao;

	// Récupérer la liste des produits

	@RequestMapping(value = "/Produits", method = RequestMethod.GET)

	public MappingJacksonValue listeProduits() {

		Iterable<Product> produits = productDao.findAll();

		SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

		FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

		MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

		produitsFiltres.setFilters(listDeNosFiltres);

		return produitsFiltres;
	}

	// Récupérer un produit par son Id
	@ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
	@GetMapping(value = "/Produits/{id}")

	public Product afficherUnProduit(@PathVariable int id) {

		Product produit = productDao.findById(id);

		if (produit == null)
			throw new ProduitIntrouvableException(
					"Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

		return produit;
	}

	// ajouter un produit
	@PostMapping(value = "/Produits")

	public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

		Product productAdded = productDao.save(product);

		if (productAdded == null)
			return ResponseEntity.noContent().build();

		if (productAdded.getPrix() - productAdded.getPrixAchat() <= 0)
			throw new ProduitGratuitException(
					"Il est interdit d'ajouter un produit dont la marge est nulle ou négative");

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(productAdded.getId()).toUri();

		return ResponseEntity.created(location).build();
	}

	@DeleteMapping(value = "/Produits/{id}")
	public void supprimerProduit(@PathVariable int id) {

		productDao.delete(id);
	}

	@PutMapping(value = "/Produits")
	public void updateProduit(@RequestBody Product product) {

		productDao.save(product);
	}

	// Pour les tests
	@GetMapping(value = "test/produits/{prix}")
	public List<Product> testeDeRequetes(@PathVariable int prix) {

		return productDao.chercherUnProduitCher(400);
	}

	@GetMapping(value = "/AdminProduits")
	public Map<Product, Integer> calculerMargeProduit() {
		HashMap<Product, Integer> lMargeList = new HashMap<Product, Integer>();
		productDao.findAll().forEach(lProduit -> {
			Integer lMarge = lProduit.getPrix() - lProduit.getPrixAchat();
			lMargeList.put(lProduit, lMarge);
		});

		return lMargeList;
	}

	@GetMapping(value = "/produits/sort")
	public List<Product> trierProduitsParOrdreAlphabetique() {
		return productDao.findAllByOrderByNomAsc();
	}

}
