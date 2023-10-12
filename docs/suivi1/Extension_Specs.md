# Extension

# Features de base de l’extension

- Adaptations des étapes A, B et C pour pouvoir : **initialiser** + instancier + **accéder** aux éléments d’un tableau

```jsx
// exemples de déclarations/instanciations
int tab[]; // déclaration d'un tableau d'entiers à une imension inconnue
float tab[6]; // déclaration d'un tableau de flottants de taille 6
String tab[3] = {"toto", "tata", "titi"}; // déclaration et instanciation d'un tableau de strings contenant 3 chaînes de caractères  
int tab[][]; // déclaration d'un tableau à 2 dimensions de dimensions inconnues
int tab[3][2]; // tableau à 3 éléments donc chaque élément correspond à un tableau à 2 éléments

// manipulations de base
float tab[3]  = {0.3, 3.2, 6.5};
float a = tab[0]; // lecture du premier élément de tab
tab[1] = 0.4; // écriture du flottant 0.4 dans le deuxième élément de tab
float tab2[3] = tab // déclaration de tab2 et assignation de tab à tab2
tab.length; // renvoie la taille du tableau 'tab' (ici égale à 3)
```

- Bibliothèque de calcul matriciel (tableaux) qu’on appellera **Tab.decah**. Une matrice de taille $n*n$ correspond ici à un tableau déclaré comme suit :

```jsx
int matrice[n][n];
```

Cette bibliothèque contient au minimum les méthodes suivantes :

```jsx
//addition de matrices
int tab[2][1]  = {{8}, {3}, {4}};
int tab2[2][1] = {{1}, {2}, {-2}};
add(tab, tab2); // effectue l'addition des coefficients de tab et tab2

//multiplication de coefficients de matrices
int tab[2][1]  = {{8}, {3}, {4}};
int tab2[2][1] = {{1}, {2}, {-2}};
multCoef(tab, tab2); // effectue la multiplication coefficient par coefficient de tab et tab2

//multiplication d'une matrice par un scalaire
int tab[2][1]  = {{8}, {3}, {4}};
multScal(tab, 7); // renvoie une matrice de taille 2*1 donc les coefficients sont ceux de tab multipliés par 7

//multiplication de matrices
int tab[2][1]  = {{2}, {3}};
int tab2[2][1] = {{-tab[1]}, {tab[0]}};
multMat(tab, tab2); // effectue la multiplication matricielle de tab2 par tab, le résultat est ici 0

//calcul de la transposée d'une matrice
int tab[2][2] = {{2,1}, {0,4}}
transpose(tab) // devrait renvoyer le tableau {{2,0}, {1,4}}
```

# Adaptation des étapes pour l’extension TAB

## Étape A

### Lexicographie

L’utilisation de tableaux nécessite l’introduction de deux nouveaux tokens que nous appellerons **OBRACKET** et **CBRACKET**, correspondant respectivement aux symboles **‘[’** et **‘]’** du clavier.

Ceci nécessite l’ajout de deux lignes supplémentaire dans le fichier `src/main/antlr4/fr/ensimag/deca/syntax/DecaParser.g4` :

```jsx
OBRACKET : '[';
CBRACKET : ']';
```

### Syntaxes concrète

- La déclaration d’un tableau se fait à l’aide des brackets ‘[]’ comme montré dans les exemples dans la section ********************************Features de base********************************. Pour prendre en compte cette nouvelle syntaxe, nous avons modifié la règle :

decl_var

→ ident 

( **'= '** expr) ?

en :

decl_var

→ ident ( ε | ( **‘[`** (INT | ε) **‘]’** )* ) 

( **'= '** expr) ?

- L’instanciation d’un tableau nécessite également l’introduction de curly brackets ‘{}’.

primary_exp

→ ident

| ident ‘(’ list_expr ') ‘

| **‘(’** expr **‘)’**

| **‘readInt’ ‘(’ ‘)’**

| **‘readFloat’ ‘(’ ‘)’**

| **‘new’** ident **‘(’ ‘)’**

| **‘(’** type **‘)’ ‘(’** expr **‘)’**

| literal

en :

primary_exp

→ ident

| **inst_tab**

| ident ‘**(’** list_expr **') ‘**

| **‘(’** expr **‘)’**

| **‘readInt’ ‘(’ ‘)’**

| **‘readFloat’ ‘(’ ‘)’**

| **‘new’** ident **‘(’ ‘)’**

| **‘(’** type **‘)’ ‘(’** expr **‘)’**

| literal

avec :

inst_tab

→ **‘{’** content_tab ( **’,’** content_tab)* **‘}’**

et :

content_tab

→ literal ************************************( **‘,’** literal )* 

| **‘{’** content_tab **‘}’**