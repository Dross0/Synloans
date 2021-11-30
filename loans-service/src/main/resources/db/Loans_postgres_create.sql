CREATE TABLE Company (
                           "id" serial NOT NULL,
                           "full_company_name" varchar(250) NOT NULL,
                           "short_company_name" varchar(255) NOT NULL,
                           "inn" varchar(10) NOT NULL UNIQUE,
                           "kpp" varchar(9) NOT NULL,
                           "legal_address" varchar(250) NOT NULL,
                           "actual_address" varchar(250) NOT NULL,
                           "ogrn" varchar(13),
                           "okpo" varchar(10),
                           "okato" varchar(10),
                           CONSTRAINT "Company_pk" PRIMARY KEY ("id")
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Contract(
                            "id" serial NOT NULL,
                            "document_id" integer NOT NULL,
                            "status" varchar(40) NOT NULL,
                            "contract_date" TIMESTAMP NOT NULL,
                            CONSTRAINT "Contract_pk" PRIMARY KEY ("id")
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Document (
                            "id" serial NOT NULL,
                            "name" varchar(255) NOT NULL,
                            "extension" varchar(20) NOT NULL,
                            "create_date" TIMESTAMP NOT NULL,
                            "last_update" TIMESTAMP NOT NULL,
                            "body" bytea NOT NULL,
                            CONSTRAINT "Document_pk" PRIMARY KEY ("id")
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Loan (
                        "id" serial NOT NULL,
                        "sum" FLOAT NOT NULL,
                        "rate" FLOAT NOT NULL,
                        "registration_date" DATE NOT NULL,
                        "close_date" DATE NOT NULL,
                        "borrower_id" integer NOT NULL,
                        CONSTRAINT "Loan_pk" PRIMARY KEY ("id")
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Loan_contracts (
                                  "loan_id" integer NOT NULL,
                                  "contract_id" integer NOT NULL
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Syndicate (
                             "id" serial NOT NULL,
                             "request" integer NOT NULL,
                             CONSTRAINT "Syndicate_pk" PRIMARY KEY ("id")
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Bank (
                        "id" serial NOT NULL,
                        "company_info" integer NOT NULL,
                        "license" integer NOT NULL,
                        CONSTRAINT "Bank_pk" PRIMARY KEY ("id")
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Syndicate_composition (
                                         "bank_id" integer NOT NULL,
                                         "syndicate_id" integer NOT NULL,
                                         "loan_sum" DECIMAL NOT NULL
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Loan_history (
                                "loan_id" integer NOT NULL,
                                "bank_id" integer NOT NULL,
                                "syndicate_id" integer
) WITH (
      OIDS=FALSE
    );



CREATE TABLE Loan_request (
                                "id" serial NOT NULL,
                                "company" integer NOT NULL,
                                "sum" integer NOT NULL,
                                "rate" integer NOT NULL,
                                CONSTRAINT "Loan_request_pk" PRIMARY KEY ("id")
) WITH (
      OIDS=FALSE
    );




ALTER TABLE Contract ADD CONSTRAINT "Contract_fk0" FOREIGN KEY ("document_id") REFERENCES Document("id");


ALTER TABLE Loan ADD CONSTRAINT "Loan_fk0" FOREIGN KEY ("borrower_id") REFERENCES Company("id");

ALTER TABLE Loan_contracts ADD CONSTRAINT "Loan_contracts_fk0" FOREIGN KEY ("loan_id") REFERENCES Loan("id");
ALTER TABLE Loan_contracts ADD CONSTRAINT "Loan_contracts_fk1" FOREIGN KEY ("contract_id") REFERENCES Contract("id");

ALTER TABLE Syndicate ADD CONSTRAINT "Syndicate_fk0" FOREIGN KEY ("request") REFERENCES Loan_request("id");

ALTER TABLE Bank ADD CONSTRAINT "Bank_fk0" FOREIGN KEY ("company_info") REFERENCES Company("id");
ALTER TABLE Bank ADD CONSTRAINT "Bank_fk1" FOREIGN KEY ("license") REFERENCES Document("id");

ALTER TABLE Syndicate_composition ADD CONSTRAINT "Syndicate_composition_fk0" FOREIGN KEY ("bank_id") REFERENCES Bank("id");
ALTER TABLE Syndicate_composition ADD CONSTRAINT "Syndicate_composition_fk1" FOREIGN KEY ("syndicate_id") REFERENCES Syndicate("id");

ALTER TABLE Loan_history ADD CONSTRAINT "Loan_history_fk0" FOREIGN KEY ("loan_id") REFERENCES Loan("id");
ALTER TABLE Loan_history ADD CONSTRAINT "Loan_history_fk1" FOREIGN KEY ("bank_id") REFERENCES Bank("id");
ALTER TABLE Loan_history ADD CONSTRAINT "Loan_history_fk2" FOREIGN KEY ("syndicate_id") REFERENCES Syndicate("id");

ALTER TABLE Loan_request ADD CONSTRAINT "Loan_request_fk0" FOREIGN KEY ("company") REFERENCES Company("id");











