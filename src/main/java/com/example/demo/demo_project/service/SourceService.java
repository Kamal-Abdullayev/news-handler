package com.example.demo.demo_project.service;

import com.example.demo.demo_project.constants.ProjectHelperConstant;
import com.example.demo.demo_project.dto.SourceDto;
import com.example.demo.demo_project.dto.SourceRequestDto;
import com.example.demo.demo_project.entity.Source;
import com.example.demo.demo_project.exception.IncompleteProcessException;
import com.example.demo.demo_project.exception.ResourceNotFoundException;
import com.example.demo.demo_project.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class SourceService {
    private final SourceRepository sourceRepository;
    private final ProjectHelperConstant projectHelperConstant;

    @Transactional
    public void saveSource(SourceRequestDto sourceDto) {
        Source source = new Source(
                sourceDto.id(),
                sourceDto.name()
        );
        SourceDto.convert(saveSourceEntity(source));
        log.info("Source saved successfully with id: {} and name: {}", source.getId(), source.getName());
    }

    protected Source saveSourceEntity(Source source) {
        sourceRepository.saveSource(source);
        log.info("Source entity saved successfully with id: {} and name: {}", source.getId(), source.getName());
        return sourceRepository.findSourceByIdAndName(source.getId(), source.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Source not found"));
    }

    @Transactional
    public void saveAllSources(List<SourceRequestDto> sourceDtoList) {
        List<Source> sourceList = sourceDtoList.stream().map(sourceDto -> new Source(
                sourceDto.id(),
                sourceDto.name()
        )).toList();
        log.info("Saving {} sources", sourceList.size());
        sourceRepository.saveAllSource(sourceList, projectHelperConstant.getSourceBatchSize());
        log.info("Sources saved successfully");
    }

    public List<SourceDto> getAllSources() {
        log.info("The sources are querying with limit: {}", projectHelperConstant.getSourceQueryLimit());
        return sourceRepository.findAllSources(projectHelperConstant.getSourceQueryLimit())
                .stream()
                .map(SourceDto::convert)
                .toList();
    }

    public SourceDto getSourceBySourceId(Long sourceId) {
        log.info("The source is querying with id: {}", sourceId);
        return SourceDto.convert(getSourceObjectBySourceId(sourceId));
    }

    protected Source getSourceObjectBySourceId(Long sourceId) {
        log.info("The source object is querying with id: {}", sourceId);
        return sourceRepository.findSourceBySourceId(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + sourceId));
    }

    public Source getSourceByIdAndName(String id, String name) {
        log.info("The source is querying with id: {} and name: {}", id, name);
        Optional<Source> isSourceExist = sourceRepository.findSourceByIdAndName(id, name);
        return isSourceExist.orElseGet(() -> saveSourceEntity(new Source(id, name)));
    }

    @Transactional
    public SourceDto updateSourceById(Long sourceId, SourceRequestDto sourceDto) {
        log.info("Updating source with id: {}", sourceId);
        Source source = getSourceObjectBySourceId(sourceId);
        if (!sourceDto.id().equals(source.getId())) {
            log.info("Source id is changed from {} to {}", source.getId(), sourceDto.id());
            source.setId(sourceDto.id());
        }
        if (!sourceDto.name().equals(source.getName())) {
            log.info("Source name is changed from {} to {}", source.getName(), sourceDto.name());
            source.setName(sourceDto.name());
        }
        Optional<Source> updatedSource = sourceRepository.updateSource(source);
        log.info("Source updated successfully with id: {}", source.getId());
        return updatedSource.map(SourceDto::convert).orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + sourceId));
    }

    @Transactional
    public void deleteSourceById(Long sourceId) {
        Source source = getSourceObjectBySourceId(sourceId);
        log.info("Deleting source with id: {}", sourceId);
        if (!sourceRepository.deleteSourceById(source.getSourceId()))
            throw new IncompleteProcessException("Problem occurred while deleting the entity with id: " + source.getSourceId());
    }
}
