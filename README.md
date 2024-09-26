![image](https://github.com/user-attachments/assets/e974dc83-f9dc-4531-afba-cd188403db36)[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=16106629&assignment_repo_type=AssignmentRepo)
# PPZ-Grasshoppers

### 1. Tytuł projektu
**"AppPub – Platforma dostępu do gier arkadowych"**
Prosta i wygodna aplikacja do grania w znane i lubiane gry arkadowe. Nie chlaj tylko graj!

### 2. Skład zespołu
**Klasa 3c:**
- **Jakub Bisikiewicz**  
- **Oliwia Grabowska**  
- **Grzegorz Kornaszewski**

### 3. Cel główny projektu
Celem aplikacji jest umożliwienie amatorskim jak i zaprawionym w bojach graczom nieograniczonego korzystania z prostych, znanych i lubianych gier arkadowych, które z pewnością przypomną użytkownikowi lata świetności.

Grupą docelową są wszyscy ludzie, którzy chcą grać w proste gry arkadowe. Nałóg zastąpi nałóg.

### 4. Zakres funkcjonalności

**Moduły:**
1. **Moduł Główny:**
   - Zestawienie i wybór gier.
   
2. **Moduł Flappy Bird:**
   - Gra Flappy Bird.
   - Modyfikacja poziomu trudności gry.
   
3. **Moduł Snake:**
   - Gra Snake.
   - Modyfikacja poziomu trudności gry.
   
4. **Moduł Pacman:**
   - Gra Pacman.
   - Modyfikacja poziomu trudności gry.

5. **Moduł Knife Hit:**
   - Gra Knife Hit.
   - Modyfikacja poziomu trudności gry.

**MVP:**
- prosta strona główna z listą gier
- co najmniej jedna gra

**Dodatkowe funkcjonalności:**
- Dla każdej gry zestawienie najlepszych wyników.

### 5. Technologie
**Języki programowania:**
   - Kotlin (Android).

**Narzędzia:**
   - Android Studio (środowisko programistyczne).
   - Git (kontrola wersji).
   - Program do projektowania grafik i interfejsu użytkownika

//TODO: completed untill here

### 6. Architektura systemu

**Diagram:**
   - Diagram powinien przedstawiać warstwy aplikacji, w tym:
     - Frontend (aplikacja mobilna),
     - Backend (serwer odpowiedzialny za zapisy i przetwarzanie kolejki),
     - Baza danych (np. Firebase/PostgreSQL).

**Opis:**
   - Aplikacja mobilna komunikuje się z serwerem (backendem), który zarządza dostępnością terminów oraz śledzi stan kolejki. Backend zapisuje dane w bazie danych, a użytkownicy otrzymują powiadomienia o zmianach kolejki za pomocą Firebase Cloud Messaging.
     
### 7. Harmonogram prac dla 4 etapów

**Etap 1: Implementacja modułu rejestracji i logowania**  
**Zadania:**
   - Projektowanie modelu użytkownika (baza danych) – *Michał Zimoń*  
   - Implementacja systemu rejestracji (formularze, walidacja) – *Norbert Waszkowiak*  
   - Implementacja logowania i uwierzytelniania (OAuth lub Firebase Auth) – *Patryk Serafin*  
   - Konfiguracja Firebase dla autoryzacji – *Patryk Serafin*  
   - Połączenie z backendem – *Norbert Waszkowiak*  
   - Testy funkcjonalne logowania i rejestracji – *Michał Zimoń*  

**Etap 2: Implementacja modułu zapisów na terminy**  
**Zadania:**
   - Stworzenie modelu danych dla terminów oddawania krwi (backend) – *Michał Zimoń*  
   - Stworzenie widoku dostępnych terminów w aplikacji – *Patryk Serafin*  
   - Mechanizm automatycznego przypisywania terminów użytkownikom – *Norbert Waszkowiak*  
   - Integracja z bazą danych (pobieranie i zapisywanie terminów) – *Michał Zimoń*  
   - Powiadomienia o potwierdzeniu zapisu – *Norbert Waszkowiak*  
   - Testy funkcjonalne zapisu na terminy – *Patryk Serafin*  

**Etap 3: Implementacja modułu kolejki**  
**Zadania:**
   - Stworzenie algorytmu śledzenia kolejki w czasie rzeczywistym – *Michał Zimoń*  
   - Backend: aktualizacja liczby osób w kolejce – *Norbert Waszkowiak*  
   - Frontend: wyświetlanie postępu kolejki – *Patryk Serafin*  
   - Powiadomienia push o zbliżającej się kolejce – *Norbert Waszkowiak*  
   - Testy działania kolejki w różnych scenariuszach – *Michał Zimoń*  

**Etap 4: Integracja modułu powiadomień i historii oddań krwi**  
**Zadania:**
   - Implementacja powiadomień push (Firebase Cloud Messaging) – *Norbert Waszkowiak*  
   - Stworzenie systemu przypomnień o nadchodzących terminach – *Patryk Serafin*  
   - Backend: zapis historii oddań krwi użytkowników – *Michał Zimoń*  
   - Frontend: wyświetlanie historii w aplikacji – *Patryk Serafin*  
   - Testy funkcji powiadomień i historii – *Michał Zimoń*  

### 8. Kryteria sukcesu
   - Zalogowani użytkownicy mogą zapisywać się na dostępne terminy oddawania krwi.
   - Powiadomienia push o zapisach i postępie kolejki działają bez opóźnień.
   - Kolejka aktualizuje się w czasie rzeczywistym.
   - Użytkownicy mogą przeglądać historię swoich oddań krwi.

### 9. Potencjalne ryzyka
   - Problemy z synchronizacją kolejki (mitigacja: regularne testowanie serwera).
   - Awaria systemu powiadomień (mitigacja: backupowe rozwiązanie oparte na SMS-ach).
   - Niska adopcja aplikacji (mitigacja: kampanie promocyjne na WAT).
