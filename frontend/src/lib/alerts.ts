import Swal from 'sweetalert2';

// Base instance themed with our design tokens so it follows light/dark mode.
const base = Swal.mixin({
  background: 'var(--color-background-primary)',
  color: 'var(--color-text-primary)',
  confirmButtonColor: 'var(--color-accent)',
  cancelButtonColor: '#6b7280',
  buttonsStyling: true,
});

/**
 * Confirmation dialog for destructive actions.
 * Resolves to `true` when the user confirms.
 */
export async function confirmDelete(name: string): Promise<boolean> {
  const result = await base.fire({
    title: 'Are you sure?',
    text: `"${name}" will be permanently deleted.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Yes, delete',
    cancelButtonText: 'Cancel',
    focusCancel: true,
  });
  return result.isConfirmed;
}

// Small auto-dismissing toast in the corner.
const toast = base.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 2200,
  timerProgressBar: true,
});

export function toastSuccess(message: string): void {
  void toast.fire({ icon: 'success', title: message });
}

export function toastError(message: string): void {
  void toast.fire({ icon: 'error', title: message });
}
