# preDict (CE) Community Edition

Spell correction fuzzy search library based on [SymSpell](https://github.com/wolfgarbe/symspell) with a few customizations and optimization:

The fundament is the Symmetric Delete spelling correction algorithm which reduces the complexity of edit candidate generation and dictionary lookup for a given edit distance. It is six orders of magnitude faster (than the standard approach with deletes + transposes + replaces + inserts) and language independent.

Additionally only deletes are required, no transposes + replaces + inserts. Transposes + replaces + inserts of the input phrase are transformed into deletes of the dictionary term. Replaces and inserts are expensive and language dependent: e.g. Chinese has 70,000 Unicode Han characters!

The main goal was to increase accuracy by adding:

* weighted Damerau-Levenshtein edit distance: each operation (delete, insert, swap, replace) can have another influence on the edit distance
* added some customizing "hooks" that are used to:
  * add several proximities algorithms (Eudex, Jaro-Winkler, dice coefficient) that are applied for the top-k results. The results are then reordered based on a combined proximity
  * added keyboard-distance to get a dynamic replacement weight (since letters close to each other are more likely to be replaced)
  * do some query normalization before search

  
## Benchmark Results

Run on Windows10 with a Intel(R) Core(TM) i7-6700 CPU (2.60GHz) with Java(TM) 1.8.0_121

```
Benchmark                             Mode  Cnt   Score   Error   Units
SearchHub with PreDict EE *          thrpt  200  86,019 ± 1,127  ops/ms
PreDict CE                           thrpt  200  82,116 ± 1,149  ops/ms
Original SymSpell (Port)             thrpt  200  68,105 ± 0,977  ops/ms
Lucene (FST Based FuzzySuggester)    thrpt    5  17,588 ± 0,690  ops/ms
Lucene (Fuzzy Field-Search)          thrpt  200   0,749 ± 0,017  ops/ms
```


## Quality Results

Based on data we collected for a few months. The test data is attached to the comparison project and can be changed. Changes to the data will, of course, change the results, but the differences shouldn't be that dramatical.

```
Benchmark                         Accuracy    TP   TN  Fail-Rate    FN   FP
SearchHub with PreDict EE *         96,86%  7452  355      3,14%   140  113
PreDict CE                          90,04%  6937  320      9,97%   496  307 
Original SymSpell (Port)            88,87%  6842  321     11,13%   591  306
Lucene (Fuzzy Field-Search)         88,87%  6803  360     11,13%   630  267
Lucene (FST based FuzzySuggester)   78,96%  5883  481     21,05%  1550  146
```

*SearchHub with PreDict EE represents our commercial offering https://www.searchhub.io
This offering is a search platform independent, AI-powered search query intelligence API containing our concept of controlled precision reduction and PreDict EE (Enterprise Edition) which is capable of handling language agnostic term decomposition 
