package com.mylogisticcba.core.service.rest;

    public class StructuredAddress {
        private String street;      // "627"
        private String city;        // "padre lozano"
        private String county;      // "cordoba"
        private String state;       // "CORDOBA"
        private String country;     // "Argentina"
        private String postalCode;  // "5000"

        public StructuredAddress() {}

        // Constructor builder-style
        public StructuredAddress street(String street) {
            this.street = street;
            return this;
        }

        public StructuredAddress city(String city) {
            this.city = city;
            return this;
        }

        public StructuredAddress county(String county) {
            this.county = county;
            return this;
        }

        public StructuredAddress state(String state) {
            this.state = state;
            return this;
        }

        public StructuredAddress country(String country) {
            this.country = country;
            return this;
        }

        public StructuredAddress postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public boolean isValid() {
            return (street != null && !street.isBlank()) ||
                    (city != null && !city.isBlank()) ||
                    (state != null && !state.isBlank()) ||
                    (country != null && !country.isBlank()) ||
                    (postalCode != null && !postalCode.isBlank());
        }

        // Getters
        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getCounty() { return county; }
        public String getState() { return state; }
        public String getCountry() { return country; }
        public String getPostalCode() { return postalCode; }

        // Setters
        public void setStreet(String street) { this.street = street; }
        public void setCity(String city) { this.city = city; }
        public void setCounty(String county) { this.county = county; }
        public void setState(String state) { this.state = state; }
        public void setCountry(String country) { this.country = country; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (street != null) sb.append(street).append(", ");
            if (city != null) sb.append(city).append(", ");
            if (county != null) sb.append(county).append(", ");
            if (state != null) sb.append(state).append(", ");
            if (country != null) sb.append(country).append(" ");
            if (postalCode != null) sb.append(postalCode);
            return sb.toString().trim();
        }
    }
