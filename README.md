# AISD-tester
Wieloplatformowa sprawdzaczka pracowni z algorytmów i struktur danych.

### Jak używać?
Sprawdzaczkę uruchamiamy poleceniem<br>
```
java -jar Sprawdzaczka.jar
```
Przy pierwszym starcie należy podać scieżkę do:
- skompilowanego programu 
- folderu zawierającego testy

### Gdzie znajdę testy?
 `https://github.com/mbronek7/AISD2018-TESTY`
### Jak określam czy program zaliczył test?
Porównuję wynik zwrócony przez program z oczekiwaną odpowiedzią.
**Ignoruję znaki nowej lini.**

### Jakie są opcjonalne argumenty?
- `-r` pozwala na zmianę ścieżki do programu i testów
- `-m` testy dla których program zawiódł będą wypisywane wraz z wartością zwróconą przez program oraz wartością oczekiwaną 
- `-c` sprawdzaczka zacznie wypisywać wszystkie testy którym poddany został program
- `-t` czas wykonywania testu
- `-h` pomoc


### Wymagania
Zainstalowany program java w wesji 10.<br>
Wpisz `java -version` aby sprawdzić jaką wersję posiadasz.
