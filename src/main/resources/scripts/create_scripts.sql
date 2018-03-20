
CREATE TABLE public."order"
(
  order_id bigint NOT NULL,
  buyer_name character varying(255),
  buyer_email character varying(255),
  order_date timestamp without time zone,
  order_total_value real,
  address character varying(255),
  postcode integer,
  CONSTRAINT order_pkey PRIMARY KEY (order_id)
)
WITH (
  OIDS=FALSE
);

CREATE TABLE public.order_item
(
  order_item_id bigint NOT NULL,
  order_id integer NOT NULL,
  sale_price real,
  shipping_price real,
  total_item_price real,
  sku character varying(255),
  status character varying(255),
  CONSTRAINT order_item_pkey PRIMARY KEY (order_item_id),
  CONSTRAINT order_fkey FOREIGN KEY (order_id)
      REFERENCES public."order" (order_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);