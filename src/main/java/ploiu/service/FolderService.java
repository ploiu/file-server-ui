package ploiu.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ploiu.client.FolderClient;

@Data
@RequiredArgsConstructor
public final class FolderService {
    @Getter(AccessLevel.NONE)
    private final FolderClient client;
}
