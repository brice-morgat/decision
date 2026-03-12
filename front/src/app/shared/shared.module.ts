import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from './material.module';
import { ReadableCodePipe } from './pipes/readable-code.pipe';

@NgModule({
  declarations: [ReadableCodePipe],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MaterialModule],
  exports: [CommonModule, FormsModule, ReactiveFormsModule, MaterialModule, ReadableCodePipe]
})
export class SharedModule {}
