# Tag System

A service that provides for the organization of abstract entities by tag. Entities are created other services and are identified by a UUID. Rather than organizing by hierarchy, tags mitigate unpleasant category duplication. Additionally, tags can be reused amongst the dependent services. 

Tags make use of inheritance, inspired by the "attribute-value tagging" of [TagStudio](https://www.youtube.com/watch?v=wTQeMkYRMcw). Effectively, tagging an entity also tags it with the the tag's sub-tags.

![tag_inheritance](https://github.com/user-attachments/assets/e891d6f8-8277-418e-b2da-6ff8025f7ad2)

In the above figure is an example of a tree of tag inheritance. If an entity is tagged with feline, searches for entities with the tags feline, mammal, or animal will retrieve it. 

## Searching

The tag system service allows for searching for entities many tags. A search may use the union or intersection operator. This means the retrieval results of each tag can be combined with either a union or intersection. Inheritance is handled by treating the subtree beneath a tag, in addition to the tag itself, as a group of tags. This group is retrieved using the union operator.

## Ownership

Entities are owned by the users that create them. This means that they have full control over how the entity is tagged. They may also delete the entity at any time. Additionally, users have no knowledge of other user's entities. 
