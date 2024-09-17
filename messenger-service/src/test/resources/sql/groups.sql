insert into t_group(id, title, description, owner_id)
values (1, 'Title 1', 'Description 1', 'j.dewar'),
       (2, 'Title 2', 'Description 2', 'j.daniels'),
       (3, 'Title 3', 'Description 3', 'j.dewar');

insert into t_group_member(id, group_id, user_id)
values (2, 1, 'j.dewar'),
       (3, 1, 'j.daniels'),
       (4, 2, 'j.daniels'),
       (5, 3, 'j.dewar');