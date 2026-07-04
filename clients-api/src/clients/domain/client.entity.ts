import { ClientSegment } from "./client-segment.enum";
import { TaxRegime } from "./tax-regime.enum";

export interface Client {
    clientId: string;
    name: string;
    segment: ClientSegment;
    taxRegime: TaxRegime;
    region: string;
}