CREATE TABLE "public.Сompany" (
	"id" serial(18) NOT NULL,
	"full_company_name" varchar(250) NOT NULL,
	"short_company_name" varchar(255) NOT NULL,
	"TIN" varchar(10) NOT NULL UNIQUE,
	"IEC" varchar(9) NOT NULL,
	"legal_address" varchar(250) NOT NULL,
	"actual_address" varchar(250) NOT NULL,
	"PSRN" varchar(13) NOT NULL,
	"OKPO" varchar(10) NOT NULL,
	"OKATO" varchar(10) NOT NULL,
	CONSTRAINT "Сompany_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Contract" (
	"id" serial(18) NOT NULL,
	"document_id" integer(18) NOT NULL,
	"status" varchar(40) NOT NULL,
	"contract_date" TIMESTAMP NOT NULL,
	CONSTRAINT "Contract_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Document" (
	"id" serial(18) NOT NULL,
	"name" varchar(255) NOT NULL,
	"extension" varchar(7) NOT NULL,
	"create_date" TIMESTAMP NOT NULL,
	"last_update" TIMESTAMP NOT NULL,
	"body" BINARY NOT NULL,
	CONSTRAINT "Document_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Loan" (
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



CREATE TABLE "public.Loan_contracts" (
	"loan_id" integer NOT NULL,
	"contract_id" integer NOT NULL
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Syndicate" (
	"id" serial NOT NULL,
	"request" integer NOT NULL,
	CONSTRAINT "Syndicate_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Bank" (
	"id" serial NOT NULL,
	"company_info" integer NOT NULL,
	"license" integer NOT NULL,
	CONSTRAINT "Bank_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Syndicate_composition" (
	"bank_id" integer NOT NULL,
	"syndicate_id" integer NOT NULL,
	"loan_sum" DECIMAL NOT NULL
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Loan_history" (
	"loan_id" integer NOT NULL,
	"bank_id" integer NOT NULL,
	"syndicate_id" integer
) WITH (
  OIDS=FALSE
);



CREATE TABLE "public.Loan_request" (
	"id" serial NOT NULL,
	"company" integer NOT NULL,
	"sum" integer NOT NULL,
	"rate" integer NOT NULL,
	CONSTRAINT "Loan_request_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);




ALTER TABLE "Contract" ADD CONSTRAINT "Contract_fk0" FOREIGN KEY ("document_id") REFERENCES "Document"("id");


ALTER TABLE "Loan" ADD CONSTRAINT "Loan_fk0" FOREIGN KEY ("borrower_id") REFERENCES "Сompany"("id");

ALTER TABLE "Loan_contracts" ADD CONSTRAINT "Loan_contracts_fk0" FOREIGN KEY ("loan_id") REFERENCES "Loan"("id");
ALTER TABLE "Loan_contracts" ADD CONSTRAINT "Loan_contracts_fk1" FOREIGN KEY ("contract_id") REFERENCES "Contract"("id");

ALTER TABLE "Syndicate" ADD CONSTRAINT "Syndicate_fk0" FOREIGN KEY ("request") REFERENCES "Loan_request"("id");

ALTER TABLE "Bank" ADD CONSTRAINT "Bank_fk0" FOREIGN KEY ("company_info") REFERENCES "Сompany"("id");
ALTER TABLE "Bank" ADD CONSTRAINT "Bank_fk1" FOREIGN KEY ("license") REFERENCES "Document"("id");

ALTER TABLE "Syndicate_composition" ADD CONSTRAINT "Syndicate_composition_fk0" FOREIGN KEY ("bank_id") REFERENCES "Bank"("id");
ALTER TABLE "Syndicate_composition" ADD CONSTRAINT "Syndicate_composition_fk1" FOREIGN KEY ("syndicate_id") REFERENCES "Syndicate"("id");

ALTER TABLE "Loan_history" ADD CONSTRAINT "Loan_history_fk0" FOREIGN KEY ("loan_id") REFERENCES "Loan"("id");
ALTER TABLE "Loan_history" ADD CONSTRAINT "Loan_history_fk1" FOREIGN KEY ("bank_id") REFERENCES "Bank"("id");
ALTER TABLE "Loan_history" ADD CONSTRAINT "Loan_history_fk2" FOREIGN KEY ("syndicate_id") REFERENCES "Syndicate"("id");

ALTER TABLE "Loan_request" ADD CONSTRAINT "Loan_request_fk0" FOREIGN KEY ("company") REFERENCES "Сompany"("id");











