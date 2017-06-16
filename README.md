# preDict

Spell correction library based on [SymSpell](https://github.com/wolfgarbe/symspell) with a few customizations and optimization:

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
PreDict EE (fast)                    thrpt  200  66,019 ± 1,427  ops/ms
Lucene (Fuzzy Field-Search)          thrpt  200   0,749 ± 0,017  ops/ms
Lucene (FST Based FuzzySuggester)    thrpt    5  17,588 ± 0,690  ops/ms
```


## Quality Results

Based on data we collected for a few months. The test data is attached to the comparison project and can be changed. Changes to the data will of course change the results, but the differences shouldn't be that dramatical.

```
Benchmark                         Accuracy    TP   TN  Fail-Rate    FN   FP
Original SymSpell (Port)            88,87%  6842  321     11,13%   591  306
PreDict CE                          90,04%  6937  320      9,97%   496  307 
SearchHub with PreDict EE           94,19%  7251  341      5,81%   182  286
Lucene (Fuzzy Field-Search)         88,87%  6803  360     11,13%   630  267
Lucene (FST based FuzzySuggester)   78,96%  5883  481     21,05%  1550  146
```
