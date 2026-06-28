import { z } from 'zod';

export const sessionSchema = z.object({
  itemId: z.coerce.number().int().min(1, "Please select an item").optional(),
  startTime: z.string().min(1, "Start time is required"),
  endTime: z.string().min(1, "End time is required"),
  reservePrice: z.preprocess(
    val => (val === '' || val === null || val === undefined) ? undefined : Number(val),
    z.number().positive("Must be > 0").optional()
  ),
  minimumIncrement: z.preprocess(
    val => (val === '' || val === null || val === undefined) ? undefined : Number(val),
    z.number().positive("Must be > 0").optional()
  ),
  antiSnipeWindowSeconds: z.coerce.number().min(0, "Cannot be negative"),
  antiSnipeExtensionSeconds: z.coerce.number().min(0, "Cannot be negative"),
  status: z.string().optional(),
  cancellationReason: z.string().optional()
}).superRefine((data, ctx) => {
  // For create mode (no status field set): itemId must be a valid positive integer
  if (!data.status && (!data.itemId || data.itemId <= 0)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: "Please select an item",
      path: ["itemId"]
    });
  }
  if (data.startTime && data.endTime) {
    if (new Date(data.endTime) <= new Date(data.startTime)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "End time must be after start time",
        path: ["endTime"]
      });
    }
  }
  if (data.status === 'CANCELLED' && (!data.cancellationReason || data.cancellationReason.trim() === '')) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: "Cancellation reason is required",
      path: ["cancellationReason"]
    });
  }
});
