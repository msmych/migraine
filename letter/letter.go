package main

import (
	"fmt"
	"html/template"
	"log"
	"net/http"
	"os"
)

var templates = template.Must(template.ParseGlob("assets/*.html"))

func main() {
	profile := os.Getenv("PROFILE")
	log.Printf("Profile: %s", profile)
	http.HandleFunc("/", indexHandler)
	http.Handle("/assets/", http.StripPrefix("/assets/", http.FileServer(http.Dir("assets"))))
	if profile == "prod" {
		port := ":8443"
		log.Printf("Starting Migraine server on %s", port)
		err := http.ListenAndServeTLS(port, "/certs/fullchain.pem", "/certs/privkey.pem", nil)
		if err != nil {
			log.Fatalf("Error starting server: %v", err)
		}
	} else {
		port := ":8080"
		log.Printf("Starting Migraine server on %s", port)
		err := http.ListenAndServe(port, nil)
		if err != nil {
			log.Fatalf("Error starting server: %v", err)
		}
	}
}

type IndexData struct {
	CdnUrl string
}

func indexHandler(w http.ResponseWriter, r *http.Request) {
	err := templates.ExecuteTemplate(w, "index.html", nil)
	if err != nil {
		http.Error(w, fmt.Sprintf("Error rendering template: %v", err), http.StatusInternalServerError)
		log.Printf("Error rendering template: %v", err)
		return
	}
}
