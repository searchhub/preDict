# preDict

Spell correction library based on [SymSpell](https://github.com/gpranav88/symspell/) with a few customizations and optimization:

* weighted Damerau-Levenshtein edit distance: each operation (delete, insert, swap, replace) can have another influence on the edit distance
* added several customizing "hooks" that are used to:
  * add several proximities algorithms (Eudex, Jaro-Winkler, dice coefficient) that are applied on the last K results. The results are then reordered based on a combined proximity
  * added keyboard-distance to get a dynamic replacement weight (since letters close to each other are more likely to be replaced)
  