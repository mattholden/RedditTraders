 create database reddittraders;
 use database reddittraders;
 
 
 
 -- Sequence: flairtemplates_flairtemplateid_seq
 
 -- DROP SEQUENCE flairtemplates_flairtemplateid_seq;
 
 CREATE SEQUENCE flairtemplates_flairtemplateid_seq
   INCREMENT 1
   MINVALUE 1
   MAXVALUE 9223372036854775807
   START 18
   CACHE 1;
 ALTER TABLE flairtemplates_flairtemplateid_seq OWNER TO postgres;
 
 
 -- Sequence: legacytrades_legacyid_seq
 
 -- DROP SEQUENCE legacytrades_legacyid_seq;
 
 CREATE SEQUENCE legacytrades_legacyid_seq
   INCREMENT 1
   MINVALUE 1
   MAXVALUE 9223372036854775807
   START 3
   CACHE 1;
 ALTER TABLE legacytrades_legacyid_seq OWNER TO postgres;
 
 
 -- Sequence: redditors_redditorid_seq
 
 -- DROP SEQUENCE redditors_redditorid_seq;
 
 CREATE SEQUENCE redditors_redditorid_seq
   INCREMENT 1
   MINVALUE 1
   MAXVALUE 9223372036854775807
   START 7
   CACHE 1;
 ALTER TABLE redditors_redditorid_seq OWNER TO postgres;
 
 
 -- Sequence: subreddits_redditid_seq
 
 -- DROP SEQUENCE subreddits_redditid_seq;
 
 CREATE SEQUENCE subreddits_redditid_seq
   INCREMENT 1
   MINVALUE 1
   MAXVALUE 9223372036854775807
   START 5
   CACHE 1;
 ALTER TABLE subreddits_redditid_seq OWNER TO postgres;
 
 
 -- Sequence: trades_tradeid_seq
 
 -- DROP SEQUENCE trades_tradeid_seq;
 
 CREATE SEQUENCE trades_tradeid_seq
   INCREMENT 1
   MINVALUE 112647
   MAXVALUE 9223372036854775807
   START 15
   CACHE 1;
 ALTER TABLE trades_tradeid_seq OWNER TO postgres;



 
 -- Table: flairtemplates
 
 -- DROP TABLE flairtemplates;
 
 CREATE TABLE flairtemplates
 (
   flairtemplateid serial NOT NULL,
   subredditid integer NOT NULL,
   mintrades integer NOT NULL DEFAULT 1,
   flairclass character varying NOT NULL,
   CONSTRAINT flairtemplates_pkey PRIMARY KEY (flairtemplateid),
   CONSTRAINT flairtemplates_subredditid_fkey FOREIGN KEY (subredditid)
       REFERENCES subreddits (redditid) MATCH SIMPLE
       ON UPDATE NO ACTION ON DELETE NO ACTION
 )
 WITH (
   OIDS=FALSE
 );
ALTER TABLE flairtemplates OWNER TO postgres;
 
 
 
 
 -- Table: legacytrades
 
 -- DROP TABLE legacytrades;
 
 CREATE TABLE legacytrades
 (
   legacyid serial NOT NULL,
   redditorid integer NOT NULL,
   subredditid integer NOT NULL,
   trades integer NOT NULL DEFAULT 0,
   CONSTRAINT legacytrades_pkey PRIMARY KEY (legacyid),
   CONSTRAINT legacytrades_redditorid_fkey FOREIGN KEY (redditorid)
       REFERENCES redditors (redditorid) MATCH SIMPLE
       ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT legacytrades_subredditid_fkey FOREIGN KEY (subredditid)
       REFERENCES subreddits (redditid) MATCH SIMPLE
       ON UPDATE NO ACTION ON DELETE NO ACTION
 )
 WITH (
   OIDS=FALSE
 );
 ALTER TABLE legacytrades OWNER TO postgres;



-- Table: redditors

-- DROP TABLE redditors;

CREATE TABLE redditors
(
  redditorid serial NOT NULL,
  username character varying(100) NOT NULL,
  CONSTRAINT redditors_pkey PRIMARY KEY (redditorid),
  CONSTRAINT redditors_username_key UNIQUE (username)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE redditors OWNER TO postgres;



-- Table: statuscodes

-- DROP TABLE statuscodes;

CREATE TABLE statuscodes
(
  statusid integer NOT NULL,
  status character varying NOT NULL,
  CONSTRAINT statuscodes_pkey PRIMARY KEY (statusid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE statuscodes OWNER TO postgres;


insert into statuscodes (statusid, status) values (1, 'Unconfirmed');
insert into statuscodes (statusid, status) values (2, 'Successful');
insert into statuscodes (statusid, status) values (3, 'Unsuccessful');
insert into statuscodes (statusid, status) values (4, 'Disputed');



-- Table: subreddits

-- DROP TABLE subreddits;

CREATE TABLE subreddits
(
  redditid serial NOT NULL,
  subreddit character varying(100) NOT NULL,
  date_added timestamp with time zone NOT NULL DEFAULT now(),
  days_between_posts integer NOT NULL DEFAULT 0,
  posts_per_day integer NOT NULL DEFAULT 1,
  count_all_subreddits boolean NOT NULL DEFAULT false,
  modflairclass character varying,
  textflair boolean NOT NULL DEFAULT false,
  activesub boolean NOT NULL DEFAULT true,
  bandays integer NOT NULL DEFAULT 0,
  banblames integer NOT NULL DEFAULT 0,
  CONSTRAINT subreddits_pkey PRIMARY KEY (redditid),
  CONSTRAINT subreddits_subreddit_key UNIQUE (subreddit)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE subreddits OWNER TO postgres;



-- Table: trades

-- DROP TABLE trades;

CREATE TABLE trades
(
  tradeid serial NOT NULL,
  redditorid1 integer NOT NULL,
  redditorid2 integer NOT NULL,
  subredditid integer NOT NULL,
  status integer NOT NULL DEFAULT 1,
  comments1 character varying(1024),
  comments2 character varying(1024),
  threadurl character varying(1024) NOT NULL,
  unsuccessful_blame_redditorid integer,
  modcomments character varying(1024),
  trade_date timestamp without time zone NOT NULL DEFAULT now(),
  resolve_date timestamp without time zone,
  CONSTRAINT trades_pkey PRIMARY KEY (tradeid),
  CONSTRAINT trades_redditorid1_fkey FOREIGN KEY (redditorid1)
      REFERENCES redditors (redditorid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT trades_redditorid2_fkey FOREIGN KEY (redditorid2)
      REFERENCES redditors (redditorid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT trades_subredditid_fkey FOREIGN KEY (subredditid)
      REFERENCES subreddits (redditid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT trades_unsuccessful_blame_redditorid_fkey FOREIGN KEY (unsuccessful_blame_redditorid)
      REFERENCES redditors (redditorid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE trades OWNER TO postgres;



-- Function: get_flair_class(character varying, character varying, boolean)

-- DROP FUNCTION get_flair_class(character varying, character varying, boolean);

CREATE OR REPLACE FUNCTION get_flair_class(puser character varying, psubreddit character varying, pismod boolean)
  RETURNS character varying AS
$BODY$

  declare	
	userid integer;
	subid integer;
	modflair character varying;
	flair character varying;
  begin
      
	select into userid redditorid from redditors where username ilike puser;
	select into subid redditid from subreddits where subreddit ilike psubreddit;
	select into modflair modflairclass from subreddits where redditid = subid;

	if (modflair is not null and pismod = true) then 
		return modflair;
	else 
		select into flair flairclass from flairtemplates where subredditid = subid and mintrades <= get_trade_count_with_countall(userid, subid) order by mintrades desc limit 1;
		return flair;
	end if;
	
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION get_flair_class(character varying, character varying, boolean) OWNER TO postgres;



-- Function: get_or_create_user(character varying)

-- DROP FUNCTION get_or_create_user(character varying);

CREATE OR REPLACE FUNCTION get_or_create_user(puser character varying)
  RETURNS integer AS
$BODY$

  declare	
	userid integer;
	
  begin
	select into userid redditorid from redditors where username ilike puser;
	if (userid is null) then
		insert into redditors (username) values (puser);
		select into userid redditorid from redditors where username ilike puser;	
	end if;
	return userid;
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION get_or_create_user(character varying) OWNER TO postgres;



-- Function: get_trade_count(integer, integer)

-- DROP FUNCTION get_trade_count(integer, integer);

CREATE OR REPLACE FUNCTION get_trade_count(prid integer, psubid integer)
  RETURNS integer AS
$BODY$

  declare	
	legacy integer;
	actual integer;
	
  begin
	select into legacy trades from legacytrades where redditorid = prid and subredditid = psubid;
	if legacy is null then legacy = 0; end if;
	select into actual count(tradeid) from trades where (redditorid1 = prid or redditorid2 = prid) and subredditid = psubid and status = 2;
	if actual is null then actual = 0; end if;
	return legacy + actual;
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION get_trade_count(integer, integer) OWNER TO postgres;



-- Function: get_trade_count_with_countall(integer, integer)

-- DROP FUNCTION get_trade_count_with_countall(integer, integer);

CREATE OR REPLACE FUNCTION get_trade_count_with_countall(prid integer, psubid integer)
  RETURNS integer AS
$BODY$

  declare	
	legacy integer;
	actual integer;
	countall boolean;
  begin
        select into countall count_all_subreddits from subreddits where redditid = psubid;

        if (countall = true) then
		select into legacy sum(trades) from legacytrades where redditorid = prid group by redditorid;
		if legacy is null then legacy = 0; end if;
		select into actual count(tradeid) from trades where (redditorid1 = prid or redditorid2 = prid) and status = 2;
		if actual is null then actual = 0; end if;
		return legacy + actual;
		
        else
		select into legacy trades from legacytrades where redditorid = prid and subredditid = psubid;
		if legacy is null then legacy = 0; end if;
		select into actual count(tradeid) from trades where (redditorid1 = prid or redditorid2 = prid) and subredditid = psubid and status = 2;
		if actual is null then actual = 0; end if;
		return legacy + actual;
	end if;
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION get_trade_count_with_countall(integer, integer) OWNER TO postgres;


-- Function: insert_trade(character varying, character varying, character varying, character varying, character varying)

-- DROP FUNCTION insert_trade(character varying, character varying, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION insert_trade(puser1 character varying, puser2 character varying, psub character varying, purl character varying, pcomments character varying)
  RETURNS integer AS
$BODY$
  declare
	id integer;
  begin
	insert into trades (redditorid1, redditorid2, subredditid, threadurl, comments1) values (get_or_create_user(puser1), get_or_create_user(puser2), (select redditid from subreddits where subreddit ilike psub), purl, pcomments) returning tradeid into id;
	
	return id;
end;	
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION insert_trade(character varying, character varying, character varying, character varying, character varying) OWNER TO postgres;


-- Function: set_legacy(character varying, character varying, integer)

-- DROP FUNCTION set_legacy(character varying, character varying, integer);

CREATE OR REPLACE FUNCTION set_legacy(puser character varying, psub character varying, ptrades integer)
  RETURNS void AS
$BODY$

  declare	
	userid integer;
	legtrades integer;
	subid integer;
  begin
	select into userid get_or_create_user(puser);
	select into subid redditid from subreddits where subreddit ilike psub;
	if (subid is null) then return; end if;

	select into legtrades trades from legacytrades where redditorid = userid and subredditid = subid;
	if (legtrades is null) then
		insert into legacytrades (redditorid, subredditid, trades) values (userid, subid, ptrades);
	else
		update legacytrades set trades = ptrades where redditorid = userid and subredditid = subid;
	end if;
	return;
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION set_legacy(character varying, character varying, integer) OWNER TO postgres;

-- Function: get_unsuccessful_count(integer, integer)

-- DROP FUNCTION get_unsuccessful_count(integer, integer);

CREATE OR REPLACE FUNCTION get_unsuccessful_count(prid integer, psubid integer)
  RETURNS integer AS
$BODY$

  declare	
	actual integer;
	
  begin
	select into actual count(tradeid) from trades where (redditorid1 = prid or redditorid2 = prid) and subredditid = psubid and status = 3;
	if actual is null then actual = 0; end if;
	return actual;
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION get_unsuccessful_count(integer, integer) OWNER TO postgres;


-- Function: get_blame_count(integer, integer)

-- DROP FUNCTION get_blame_count(integer, integer);

CREATE OR REPLACE FUNCTION get_blame_count(prid integer, psubid integer)
  RETURNS integer AS
$BODY$

  declare		
	actual integer;
	
  begin
	select into actual count(tradeid) from trades where unsuccessful_blame_redditorid = prid and subredditid = psubid and status = 3;	
	if actual is null then actual = 0; end if;
	return actual;
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION get_blame_count(integer, integer) OWNER TO postgres;


-- Function: should_ban(integer, integer)

-- DROP FUNCTION should_ban(integer, integer);

CREATE OR REPLACE FUNCTION should_ban(prid integer, psubid integer)
  RETURNS boolean AS
$BODY$

  declare		
	ban_blames integer;
	ban_days integer;
	countall boolean;
	blamez integer;
	
  begin
	select into ban_blames banblames from subreddits where redditid = psubid;
	select into ban_days bandays from subreddits where redditid = psubid;
	select into countall count_all_subreddits from subreddits where redditid = psubid;
	
	if (countall = true) then 
		select into blamez count(tradeid) from trades where unsuccessful_blame_redditorid = prid and resolve_date >= (now() - ban_days * (interval '1 day'));
	else
		select into blamez count(tradeid) from trades where unsuccessful_blame_redditorid = prid and subredditid = psubid and resolve_date >= (now() - ban_days * (interval '1 day'));
	end if;
	
	if (blamez >= ban_blames) then
		return true;
	else
		return false;
	end if;
	
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION should_ban(integer, integer) OWNER TO postgres;


