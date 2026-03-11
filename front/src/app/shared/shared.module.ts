import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from './material.module';
import { ReadableCodePipe } from './pipes/readable-code.pipe';

@NgModule({
  declarations: [ReadableCodePipe],
  imports: [CommonModule, ReactiveFormsModule, MaterialModule],
  exports: [CommonModule, ReactiveFormsModule, MaterialModule, ReadableCodePipe]
})
export class SharedModule {}
