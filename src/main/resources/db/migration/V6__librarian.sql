insert into app_user (username, password_hash) values
    ('librarian', '$2a$10$CX.rgm9QdP4Eja3ljTA4yeHTCgZmmsB.JwdPaZGEnam8yDXypn6pq');

insert into user_role (user_id, role)
select id, 'USER' from app_user where username = 'librarian';

insert into user_role (user_id, role)
select id, 'LIBRARIAN' from app_user where username = 'librarian';