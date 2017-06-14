# preDict

Spell correction library based on [SymSpell](https://github.com/gpranav88/symspell/) with a few customizations and optimization:

* weighted Damerau-Levenshtein edit distance: each operation (delete, insert, swap, replace) can have another influence on the edit distance
* added some customizing "hooks" that are used to:
  * add several proximities algorithms (Eudex, Jaro-Winkler, dice coefficient) that are applied on the last K results. The results are then reordered based on a combined proximity
  * added keyboard-distance to get a dynamic replacement weight (since letters close to each other are more likely to be replaced)
  * do some query normalization before search

  
## Benchmark Results

Run on Windows10 with a Intel(R) Core(TM) i7-6700 CPU (2.60GHz) with Java(TM) 1.8.0_121

```
Benchmark                             Mode  Cnt   Score   Error   Units
Original SymSpell (Port)             thrpt  200  68,105 ± 0,977  ops/ms
PreDict CE                           thrpt  200  82,116 ± 1,149  ops/ms
Lucene (Fuzzy Field-Search)          thrpt  200   0,749 ± 0,017  ops/ms
Lucene (FST Based FuzzySuggester)    thrpt    5  17,588 ± 0,690  ops/ms
```


## Quality Results

Based on data we collected for a few months. The test data is attached to the comparison project and can be changed. Changes to the data will of course change the results, but the differences shouldn't be that dramatical.

```
Benchmark                         Accuracy    TP   TN  Fail-Rate    FN   FP
Original SymSpell (Port)            88,88%  6847  321     11,12%   591  306
PreDict CE                          90,08%  6944  321      9,97%   494  306 
Lucene (Fuzzy Field-Search)         88,92%  6810  361     11,08%   628  266
Lucene (FST based FuzzySuggester)   78,95%  5886  481     21,05%  1552  146
```
