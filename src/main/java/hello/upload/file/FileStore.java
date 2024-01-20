package hello.upload.file;

import hello.upload.domain.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if(!multipartFile.isEmpty()){
                UploadFile uploadFile = storeFile(multipartFile);
                storeFileResult.add(uploadFile);
            }
        }

        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename(); //image.png

        InputStream inputStream = multipartFile.getInputStream();
        String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        log.info("content = {}", content);

        //서버에 저장할 파일명 생성
        String storeFileName = createStoreFileName(originalFilename);

        //내 컴퓨터에 저장
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        // 서버에 저장할 객체 반환
        return new UploadFile(originalFilename, storeFileName);

    }

    //서버에 저장할 파일명 (중복되지 않게 uuid로 만들고 확장자만 붙여줌
    private static String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        String storeFileName = uuid + "." + ext;

        return storeFileName;
    }

    //확장자 추출 메서드
    private static String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf("."); //파일명의 마지막 .이 나온 인덱스 값
        String ext = originalFilename.substring(pos + 1); //확장자 png
        return ext;
    }

}
