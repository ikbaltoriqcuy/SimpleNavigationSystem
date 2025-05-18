import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

class City {
  String name;

  public City(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}

class Route {
  City destination;
  int distance; // dalam km
  int time; // dalam menit
  double fuel; // dalam liter

  public Route(City destination, int distance, int time, double fuel) {
    this.destination = destination;
    this.distance = distance;
    this.time = time;
    this.fuel = fuel;
  }
}

class NavigationSystem {
  private Map<City, List<Route>> graph = new HashMap<>();

  public Map<City, List<Route>> getCities() {
    return graph;
  }

  // Tambahkan kota ke graph
  public void addCity(City city) {
    graph.putIfAbsent(city, new ArrayList<>());
  }

  // Tambahkan rute antara dua kota
  public void addRoute(City source, City destination, int distance, int time, double fuel) {
    graph.get(source).add(new Route(destination, distance, time, fuel));
    graph.get(destination).add(new Route(source, distance, time, fuel)); // untuk rute dua arah
  }

  // Cari rute terpendek berdasarkan parameter (distance, time, atau fuel)
  public void findShortestPath(City start, City end, String parameter) {
    Map<City, Double> distances = new HashMap<>();
    Map<City, City> previous = new HashMap<>();
    PriorityQueue<City> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

    // Inisialisasi
    for (City city : graph.keySet()) {
      if (city.equals(start)) {
        distances.put(city, 0.0);
      } else {
        distances.put(city, Double.MAX_VALUE);
      }
      previous.put(city, null);
    }

    queue.add(start);

    while (!queue.isEmpty()) {
      City current = queue.poll();

      if (current.equals(end)) {
        break;
      }

      for (Route route : graph.get(current)) {
        double value;
        switch (parameter.toLowerCase()) {
          case "time":
            value = route.time;
            break;
          case "fuel":
            value = route.fuel;
            break;
          default: // default distance
            value = route.distance;
        }

        double newDistance = distances.get(current) + value;
        if (newDistance < distances.get(route.destination)) {
          distances.put(route.destination, newDistance);
          previous.put(route.destination, current);
          queue.add(route.destination);
        }
      }
    }

    // Bangun path
    List<City> path = new ArrayList<>();
    for (City at = end; at != null; at = previous.get(at)) {
      path.add(at);
    }
    Collections.reverse(path);

    // Hitung total
    int totalDistance = 0;
    int totalTime = 0;
    double totalFuel = 0;

    for (int i = 0; i < path.size() - 1; i++) {
      City from = path.get(i);
      City to = path.get(i + 1);

      for (Route route : graph.get(from)) {
        if (route.destination.equals(to)) {
          totalDistance += route.distance;
          totalTime += route.time;
          totalFuel += route.fuel;
          break;
        }
      }
    }

    // Tampilkan hasil
    System.out.println("\nRute terpendek berdasarkan " + parameter + ":");
    System.out.println(String.join(" -> ", path.stream().map(City::toString).toArray(String[]::new)));
    System.out.println("Total Jarak: " + totalDistance + " km");
    System.out
        .println("Total Waktu: " + totalTime + " menit (" + (totalTime / 60) + " jam " + (totalTime % 60) + " menit)");
    System.out.println("Total Bensin: " + String.format("%.2f", totalFuel) + " liter");
  }
}

public class Main {
  public static void main(String[] args) {
    NavigationSystem nav = new NavigationSystem();

    // Buat kota-kota
    City jakarta = new City("Jakarta");
    City bandung = new City("Bandung");
    City surabaya = new City("Surabaya");
    City yogyakarta = new City("Yogyakarta");
    City semarang = new City("Semarang");

    // Tambahkan kota ke sistem
    nav.addCity(jakarta);
    nav.addCity(bandung);
    nav.addCity(surabaya);
    nav.addCity(yogyakarta);
    nav.addCity(semarang);

    // Tambahkan rute (jarak km, waktu menit, bensin liter)
    nav.addRoute(jakarta, bandung, 150, 180, 12.5); // Jakarta-Bandung
    nav.addRoute(bandung, yogyakarta, 400, 480, 33.3); // Bandung-Yogyakarta
    nav.addRoute(yogyakarta, surabaya, 325, 390, 27.1); // Yogyakarta-Surabaya
    nav.addRoute(jakarta, semarang, 450, 540, 37.5); // Jakarta-Semarang
    nav.addRoute(semarang, surabaya, 350, 420, 29.2); // Semarang-Surabaya
    nav.addRoute(bandung, semarang, 300, 360, 25.0); // Bandung-Semarang

    try (Scanner scanner = new Scanner(System.in)) {
      System.out.println("Sistem Navigasi Antar Kota");
      System.out.println("Kota yang tersedia:");
      nav.getCities().keySet().forEach(city -> System.out.println("- " + city.name));

      System.out.print("\nMasukkan kota asal: ");
      String startCity = scanner.nextLine();
      System.out.print("Masukkan kota tujuan: ");
      String endCity = scanner.nextLine();

      AtomicReference<City> start = new AtomicReference<>();
      AtomicReference<City> end = new AtomicReference<>();

      // Cari objek kota berdasarkan nama
      nav.getCities().forEach((city, _) -> {
        if (city.name.equalsIgnoreCase(startCity)) {
          start.set(city);
        }
        if (city.name.equalsIgnoreCase(endCity)) {
          end.set(city);
        }
      });

      if (start.get() == null || end.get() == null) {
        System.out.println("Kota tidak ditemukan!");
        return;
      }

      System.out.println("\nPilih parameter pencarian rute terpendek:");
      System.out.println("1. Jarak terpendek");
      System.out.println("2. Waktu tercepat");
      System.out.println("3. Konsumsi bensin paling irit");
      System.out.print("Pilihan (1-3): ");
      int choice = scanner.nextInt();

      switch (choice) {
        case 1:
          nav.findShortestPath(start.get(), end.get(), "distance");
          break;
        case 2:
          nav.findShortestPath(start.get(), end.get(), "time");
          break;
        case 3:
          nav.findShortestPath(start.get(), end.get(), "fuel");
          break;
        default:
          System.out.println("Pilihan tidak valid!");
      }
    }
  }
}