[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=16106629&assignment_repo_type=AssignmentRepo)
# ppz-Grasshoppers

### 1. Tytuł projektu
**"KrewApp – Zapis na Oddawanie Krwi na WAT"**
Prosta i wygodna aplikacja do automatyzacji procesu rejestracji na oddawanie krwi oraz śledzenia postępu kolejki.

### 2. Skład zespołu
**Klasa 3c:**
- **Jakub Bisikiewicz**  
- **Oliwia Grabowska**  
- **Grzegorz Kornaszewski**

### 3. Cel główny projektu
Celem aplikacji jest ułatwienie procesu zapisywania się na oddawanie krwi na Wojskowej Akademii Technicznej (WAT) oraz informowanie użytkownika o postępie kolejki w czasie rzeczywistym. Zamiast ręcznego wysyłania maili, aplikacja zautomatyzuje proces zapisów i zapewni wygodę poprzez dostarczanie użytkownikowi bieżących informacji na temat statusu kolejki, co pozwoli na optymalne wykorzystanie czasu i zminimalizowanie oczekiwania.

Grupą docelową są studenci i pracownicy WAT oraz inne osoby biorące udział w akcjach krwiodawstwa organizowanych na uczelni.

### 4. Zakres funkcjonalności

**Moduły:**
1. **Moduł Rejestracji i Logowania**:  
   - Rejestracja użytkowników i logowanie przez numer telefonu lub e-mail.  
   
2. **Moduł Zapisów**:  
   - Zapis na dostępne terminy oddawania krwi, automatyczny wybór z dostępnych terminów.  
   - Powiadomienia o zapisach oraz nadchodzących terminach.  
   
3. **Moduł Kolejki**:  
   - Bieżące informacje o liczbie osób oczekujących przed użytkownikiem.  
   - Aktualizacja kolejki w czasie rzeczywistym.  
   
4. **Moduł Powiadomień**:  
   - Powiadomienia push o nowych terminach, potwierdzeniu zapisu oraz przypomnienia o oddawaniu krwi.  

**Dodatkowe funkcjonalności:**
- Historia oddań krwi i statystyki, np. liczba oddanej krwi.
- Ranking krwiodawców z największą liczbą oddań.
- Możliwość anulowania rezerwacji i wybierania nowych terminów.

**MVP:**  
- Rejestracja i logowanie.  
- Zapis na termin oddawania krwi.  
- Wyświetlanie liczby osób w kolejce przed użytkownikiem.

### 5. Technologie
**Języki programowania:**
   - Kotlin (Android).
   - Java (backend).

**Frameworki:**
   - SpringBoot

**Narzędzia:**
   - Firebase (autoryzacja, baza danych, powiadomienia).
   - Git (kontrola wersji).
   - PostgreSQL (baza danych).

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
