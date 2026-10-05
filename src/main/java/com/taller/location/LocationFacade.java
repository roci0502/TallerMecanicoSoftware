package com.taller.location;

import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

@Service
public class LocationFacade {
    private final SepomexCatalogRepository catalogRepository;
    private volatile List<SepomexCatalogRepository.AddressEntry> catalog;
    public LocationFacade(SepomexCatalogRepository catalogRepository) { this.catalogRepository = catalogRepository; }

    public List<StateOption> states() { return entries().stream().collect(java.util.stream.Collectors.toMap(SepomexCatalogRepository.AddressEntry::stateCode, entry -> entry.state(), (first, ignored) -> first, TreeMap::new)).entrySet().stream().map(entry -> new StateOption(entry.getKey(), entry.getValue())).toList(); }
    public List<MunicipalityOption> municipalities(String stateCode) { return entries().stream().filter(entry -> entry.stateCode().equals(stateCode)).collect(java.util.stream.Collectors.toMap(SepomexCatalogRepository.AddressEntry::municipalityCode, entry -> entry.municipality(), (first, ignored) -> first, TreeMap::new)).entrySet().stream().map(entry -> new MunicipalityOption(entry.getKey(), entry.getValue())).toList(); }
    public List<NeighborhoodOption> neighborhoods(String stateCode, String municipalityCode) {
        Map<String, TreeSet<String>> neighborhoods = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        entries().stream().filter(entry -> entry.stateCode().equals(stateCode) && entry.municipalityCode().equals(municipalityCode)).forEach(entry -> neighborhoods.computeIfAbsent(entry.neighborhood(), ignored -> new TreeSet<>()).add(entry.postalCode()));
        return neighborhoods.entrySet().stream().map(entry -> new NeighborhoodOption(entry.getKey(), List.copyOf(entry.getValue()))).sorted(Comparator.comparing(NeighborhoodOption::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }
    private List<SepomexCatalogRepository.AddressEntry> entries() { List<SepomexCatalogRepository.AddressEntry> current = catalog; if (current == null) synchronized (this) { if (catalog == null) catalog = List.copyOf(catalogRepository.download()); current = catalog; } return current; }
    public record StateOption(String code, String name) { }
    public record MunicipalityOption(String code, String name) { }
    public record NeighborhoodOption(String name, List<String> postalCodes) { }
}
