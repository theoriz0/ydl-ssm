package com.ydlclass.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class YdlBaseEntity implements Serializable {
    private static final long serialVersionId = 1L;

    private int page;
    private int size;
    private Sort sort;
}
